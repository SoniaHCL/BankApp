# BankApp 🏦

Welcome to **BankApp**, a simple financial sample application designed to handle deposits, withdrawals, and transaction tracking. 

This repository serves as a practical demonstration of **[Code Genie](https://help.hcl-software.com/devops/loop/2.0.1/docs/code/code_genie.html)**  in action, showcasing how it can be utilized to automate and enhance unit testing.
Blog link:

---

## 🛠️ Tech Stack & Architecture

* **Backend:** Java 
* **Build Tool:** Maven (configured via `pom.xml`)
* **Testing Framework:** JUnit

---

## 📦 Repository Structure

The public repository contains all the essential components to get started:
All Java source files and testing code are neatly organized inside the standard Maven `src` directory

* 📄 **`Account.java`**: The core backend business logic managing bank account operations.
* 🧪 **`AccountTest.java`**: The testing suite containing **6 basic unit tests** powered by JUnit and Code Genie.
* ⚙️ **`pom.xml`**: The Maven configuration file detailing necessary project dependencies.

There is a folder **devcontainer build files**, which contains reference files for the Code Genie environment:

- Dockerfile: Setup for Java 17, Maven, GCM, and tooling
- code-config.jsonc: JSON configuration instructing Code Genie on what to execute
- devcontainer.json: Environment-specific dev container configuration 
- run-junit.sh: Helper automation runner script for JUnit reporting

---
