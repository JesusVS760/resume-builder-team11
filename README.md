# Tailored Resume Builder

```
TailoredResumeBuilder/
├── build/                     # Compiled .class files (generates the folder with the .class files in it locally when running)
├── database/                  # SQLite database (only local for testing, creates database.db the in folder when running)
├── uploads/                   # Folder is created locally when running program, stores saved resumes
├── lib/                       # External JAR libraries
├── meetingnotes/              # Meeting notes
├── resources/                 # Database schema and resources
├── src/                       # Source code directory
│   ├── Main.java              # Application entry point
│   ├── models/                # Data models (User, Resume, JobDescription, TailoredResume)
│   ├── dao/                   # Database access layer
│   ├── services/              # Business logic layer
│   ├── ui/                    # Java Swing UI components
│   └── utils/                 # Utility classes
├── .gitignore                 # Git ignore file
├── README.md                  # Project documentation
├── run.bat                    # Windows run script
└── run.sh                     # Mac/Linux run script
```

## How to run on Windows (using run.bat file)

.\run.bat

## How to run on Mac (using run.sh file)

chmod +x run.sh (only have to run this once)

./run.sh

## Install SQLite if not already installed

### Windows
1. Open a browser and go to the official SQLite download page: `https://www.sqlite.org/download.html`.
2. Under **"Precompiled Binaries for Windows"**, download the file named **`sqlite-tools-win-x64-<version>.zip`**.
3. Create a folder in C:\ called sqlite and extract the zip file to: `C:\sqlite`.
4. Add this folder to your **PATH** so you can run `sqlite3` from any terminal:
   - Open the Start menu and search for **"Environment Variables"**, then click **"Edit the system environment variables"**.
   - Click **Environment Variables...**.
   - Under **System variables**, select **Path** and click **Edit...**.
   - Click **New** and add the path to the folder where you extracted SQLite (`C:\sqlite`).
   - Click **OK** on all dialogs to save.
5. Open a new **Command Prompt** or **PowerShell** window and run:
   - `sqlite3 --version`
   - If you see a version number, SQLite is installed correctly.

### macOS
1. Open **Terminal**.
2. If you have **Homebrew** installed, run:
   - `brew install sqlite`
3. If you do **not** have Homebrew, you can install it by running (from `https://brew.sh`):
   - `/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"`
   - Then run `brew install sqlite`.
4. Verify the installation by running in Terminal:
   - `sqlite3 --version`
   - If you see a version number, SQLite is installed correctly.

## Database Access

**Open database, make sure you are in project root**


sqlite3 database/database.db


**View tables**


.tables


**View data of users who sign up normally**


SELECT * FROM users;


**View data of users who signed up using oAuth**

SELECT * FROM oauth_users;

**View saved resumes**

SELECT * FROM resumes;


**Exit SQLite**

.quit


