# Tailored Resume Builder

```
TailoredResumeBuilder/
├── src/                                   # Source code directory
│   ├── Main.java                          # Application entry point
│   ├── models/                            # Data models (User, Resume, JobDescription, TailoredResume)
│   ├── dao/                               # Database access layer
│   ├── services/                          # Business logic layer
│   ├── ui/                                # Java Swing UI components
│   └── utils/                             # Utility classes
├── build/                                 # Compiled .class files (generated)
├── homepage/                              # Homepage integration
├── lib/                                   # External JAR libraries
├── database/                              # SQLite database (only local for testing, creates database.db in folder when running) 
└── resources/                             # Database schema and resources
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

