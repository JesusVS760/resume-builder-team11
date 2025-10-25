# Tailored Resume Builder

```
TailoredResumeBuilder/
├── build/                     # Compiled .class files (generates the folder with the .class files in it locally when running)
├── database/                  # SQLite database (only local for testing, creates database.db the in folder when running)
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

# How to run on Windows(using run.bat file)

.\run.bat

# How to run on Mac (using run.sh file)

chmod +x run.sh (only have to run this once)

./run.sh

# Database Access

**Open database, make sure you are in project root**


sqlite3 database/database.db


**View tables**


.tables


**View all user data**


SELECT * FROM users;


**Exit SQLite**

.quit

