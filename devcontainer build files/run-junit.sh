#!/usr/bin/env bash
set -uo pipefail

# ─────────────────────────────────────────────
# 1. Capture Timestamps
# ─────────────────────────────────────────────
START_TIME=$(date -u +"%Y-%m-%dT%H:%M:%SZ")

# ─────────────────────────────────────────────
# 2. Defaults & State Variables
# ─────────────────────────────────────────────
PROJECT_DIR=""
OUTPUT_JSON=""
REPO_URL="${REPO_URL:-}"
RESULT_DIR="/tmp/junit-results"
REJECT_ON_FAILURE="true"     # --reject=never disables pipeline rejection
REJECT_ERROR=""

# ─────────────────────────────────────────────
# 3. Argument Parser  (mirrors Trivy layout)
# ─────────────────────────────────────────────
for arg in "$@"; do
    case $arg in
        --reject=*)
            val="${arg#*=}"; val="${val^^}"
            if [[ "$val" == "NEVER" ]]; then
                REJECT_ON_FAILURE="false"
            elif [[ "$val" == "ALWAYS" || "$val" == "TRUE" ]]; then
                REJECT_ON_FAILURE="true"
            else
                REJECT_ERROR="Invalid --reject value: '$val'. Expected ALWAYS or NEVER."
            fi
            ;;
        --resultDir=*)
            RESULT_DIR="${arg#*=}"
            ;;
        --repoUrl=*)
            REPO_URL="${arg#*=}"
            ;;
        *)
            if [ -z "$PROJECT_DIR" ]; then PROJECT_DIR="$arg"
            elif [ -z "$OUTPUT_JSON" ]; then OUTPUT_JSON="$arg"; fi
            ;;
    esac
done

if [ -z "$PROJECT_DIR" ] || [ -z "$OUTPUT_JSON" ]; then
    echo "USAGE: $0 <PROJECT_DIR> <OUTPUT_JSON> [--reject=ALWAYS|NEVER] [--resultDir=<path>] [--repoUrl=<url>]" >&2
    exit 1
fi

# ─────────────────────────────────────────────
# 4. Tool & Directory Validation
# ─────────────────────────────────────────────
if ! command -v mvn &>/dev/null; then
    echo "run-junit-tests: ❌ Error: 'mvn' not found in PATH." >&2
    exit 1
fia

if ! command -v jq &>/dev/null; then
    echo "run-junit-tests: ❌ Error: 'jq' not found in PATH (required for JSON output)." >&2
    exit 1
fi

if [ ! -d "$PROJECT_DIR" ]; then
    echo "run-junit-tests: ❌ Error: Project directory '$PROJECT_DIR' not found." >&2
    exit 1
fi

if [ ! -f "$PROJECT_DIR/pom.xml" ]; then
    echo "run-junit-tests: ❌ Error: No pom.xml found in '$PROJECT_DIR'. Is this a Maven project?" >&2
    exit 1
fi

# ─────────────────────────────────────────────
# 5. Execution
# ─────────────────────────────────────────────
SUREFIRE_TMP=$(mktemp)
ERROR_LOG=$(mktemp)
MAVEN_EXIT_CODE=0
VERBATIM_ERROR=""
ERROR_LABEL="Maven stderr"
INSTRUCTION_MSG="Please check the Maven output and test logs for details."

echo "run-junit-tests: ⏳ Started: $START_TIME"
echo "run-junit-tests: 🌐 Repository Target: ${REPO_URL:-<none>}"
echo "run-junit-tests: 📁 Project Dir: $PROJECT_DIR"

if [ -n "$REJECT_ERROR" ]; then
    VERBATIM_ERROR="$REJECT_ERROR"
    MAVEN_EXIT_CODE=1
    ERROR_LABEL="Argument error"
    INSTRUCTION_MSG="Please verify the --reject flag value."
else
    cd "$PROJECT_DIR"

    # Run Maven; capture stderr separately; never let set -o pipefail kill us here
    mvn clean test --batch-mode 2>"$ERROR_LOG" || MAVEN_EXIT_CODE=$?

    # Copy Surefire XML reports out for CI archiving
    if [ -d "target/surefire-reports" ]; then
        mkdir -p "$RESULT_DIR"
        cp -r target/surefire-reports/. "$RESULT_DIR"/
    fi

    # Aggregate metrics from Surefire XML (robust vs. parsing .txt with awk)
    # Produces a single JSON object: {"tests":N,"failures":N,"errors":N,"skipped":N,"files":[...]}
    python3 - "$RESULT_DIR" <<'PYEOF' > "$SUREFIRE_TMP" 2>>"$ERROR_LOG" || true
import sys, os, json, xml.etree.ElementTree as ET

result_dir = sys.argv[1]
totals = {"tests": 0, "failures": 0, "errors": 0, "skipped": 0}
cases  = []

if os.path.isdir(result_dir):
    for fname in os.listdir(result_dir):
        if not fname.endswith(".xml") or not fname.startswith("TEST-"):
            continue
        fpath = os.path.join(result_dir, fname)
        try:
            tree = ET.parse(fpath)
            root = tree.getroot()
            suite = root if root.tag == "testsuite" else root.find("testsuite")
            if suite is None:
                continue
            totals["tests"]    += int(suite.get("tests",    0))
            totals["failures"] += int(suite.get("failures", 0))
            totals["errors"]   += int(suite.get("errors",   0))
            totals["skipped"]  += int(suite.get("skipped",  0))
            for tc in suite.findall("testcase"):
                failure = tc.find("failure")
                error   = tc.find("error")
                skipped = tc.find("skipped")
                if failure is not None or error is not None:
                    node = failure if failure is not None else error
                    cases.append({
                        "classname": tc.get("classname", "Unknown"),
                        "name":      tc.get("name", "Unknown"),
                        "kind":      "FAILURE" if failure is not None else "ERROR",
                        "message":   (node.get("message") or node.text or "")[:300].strip()
                    })
        except Exception as e:
            pass  # malformed XML; skip silently

print(json.dumps({"totals": totals, "failed_cases": cases}))
PYEOF

    # Capture first meaningful Maven error line for the comment when Maven itself failed
    if [ "$MAVEN_EXIT_CODE" -ne 0 ] && [ ! -s "$SUREFIRE_TMP" ]; then
        VERBATIM_ERROR=$(grep -E "\[ERROR\]|FATAL|BUILD FAILURE" "$ERROR_LOG" | head -n 1 | sed 's/\[ERROR\] //' | tr '\t' ' ' || echo "Unknown Maven error")
    fi
fi

END_TIME=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
echo "run-junit-tests: 🏁 Ended: $END_TIME"

# ─────────────────────────────────────────────
# 6. JSON Output  (mirrors Trivy's jq block)
# ─────────────────────────────────────────────
jq -n \
  --slurpfile surefire "$SUREFIRE_TMP" \
  --arg reject_on_failure "$REJECT_ON_FAILURE" \
  --arg start_time        "$START_TIME" \
  --arg end_time          "$END_TIME" \
  --arg repo_url          "$REPO_URL" \
  --arg exit_code         "$MAVEN_EXIT_CODE" \
  --arg verbatim_err      "$VERBATIM_ERROR" \
  --arg err_label         "$ERROR_LABEL" \
  --arg instruction       "$INSTRUCTION_MSG" \
  '
  # ── helpers ──────────────────────────────────────────────────
  def make_link(p):
    if ($repo_url != "") then ("[" + p + "](<" + $repo_url + p + ">)") else p end;

  # ── parse Surefire payload (guard against empty slurp) ───────
  (($surefire | .[0]) // {"totals":{"tests":0,"failures":0,"errors":0,"skipped":0},"failed_cases":[]}) as $sf |
  ($sf.totals.tests    // 0) as $total    |
  ($sf.totals.failures // 0) as $failures |
  ($sf.totals.errors   // 0) as $errors   |
  ($sf.totals.skipped  // 0) as $skipped  |
  ($sf.failed_cases    // []) as $cases   |

  # ── derive overall pass/fail ──────────────────────────────────
  (($exit_code != "0") or ($failures > 0) or ($errors > 0)) as $tests_failed |
  ($reject_on_failure == "true" and $tests_failed) as $is_rejected |

  # ── build per-failure inline comment rows ────────────────────
  ($cases | map({
      path: (.classname | gsub("\\."; "/")),
      new_position: 1,
      body: (
        "[" + .kind + "] " + .classname + "#" + .name +
        (if .message != "" then " | " + .message else "" end)
      )
  })) as $results |

  # ── assemble output (same shape as Trivy script) ─────────────
  {
    "results": $results,

    "pr-comments": (
      if $exit_code != "0" and ($total == 0) then
        [
          "❌ **Maven Build / Test Execution Failed**",
          ($err_label + ": " + $verbatim_err),
          $instruction
        ]
      else
        [
          ("Started: " + $start_time + ", Ended: " + $end_time),
          ("Test summary => Total: " + ($total|tostring) +
           ", Passed: " + (($total - $failures - $errors - $skipped)|tostring) +
           ", Failures: " + ($failures|tostring) +
           ", Errors: " + ($errors|tostring) +
           ", Skipped: " + ($skipped|tostring)),
          "",
          (if $reject_on_failure == "false" then
             "✅ JUnit scan completed (Rejection disabled)."
           elif $is_rejected then
             "⚠️ **Test failures detected. Please fix failing tests before merging.**"
           else
             "✅ All JUnit tests passed."
           end),
          (if ($cases | length) > 0 then "🧪 Failed Test Details" else empty end),
          (if ($cases | length) > 0 then "| Class | Test | Kind | Message |" else empty end),
          (if ($cases | length) > 0 then "| :--- | :--- | :--- | :--- |" else empty end),
          (if ($cases | length) > 0 then
             ($cases[] | "| " + .classname + " | " + .name + " | " + .kind + " | " + .message + " |")
           else
             "✅ No test failures."
           end)
        ]
      end
    ),

    "review": {
      "approved": (
        if $exit_code != "0" and ($total == 0) then false
        elif $reject_on_failure == "false" then true
        elif $is_rejected then false
        else true
        end
      ),
      "comment": (
        if $exit_code != "0" and ($total == 0) then
          ("Build failed: " + $verbatim_err)
        elif $reject_on_failure == "false" then
          "Rejection disabled: Auto-approved."
        elif $is_rejected then
          ("Rejected: " + ($failures|tostring) + " failure(s), " + ($errors|tostring) + " error(s) in test suite.")
        else
          ("All " + ($total|tostring) + " JUnit tests passed.")
        end
      )
    }
  }
  ' > "$OUTPUT_JSON"

# ─────────────────────────────────────────────
# 7. Cleanup & Exit
# ─────────────────────────────────────────────
rm -f "$SUREFIRE_TMP" "$ERROR_LOG"

echo "run-junit-tests: ✅ Result JSON written to $OUTPUT_JSON"
exit "$MAVEN_EXIT_CODE"
