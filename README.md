# ZERO TO HUNDRED'S HUNT
### A Complete Filmmaker Development & Creative Workspace Platform

**Zero to Hundred's Hunt** is an enterprise-grade, full-stack creative workspace engineered for aspiring filmmakers, independent directors, screenwriters, and creative producers. The application facilitates the entire creative journey: transforming an initial concept into structured screenplays, tracking character arcs and scene breakdowns, engaging with an AI Dramaturg mentor, scheduling multi-stage film productions, archiving cinematic observations, and preserving raw ideas in a creative vault.

---

## 1. TECHNOLOGY STACK

- **Frontend:**
  - Modern Semantic HTML5
  - Cinematic CSS3 (Film Noir theme, custom variables, responsive grid/flexbox)
  - Vanilla JavaScript (Native `fetch`, modular architectures, zero external frontend framework dependencies)

- **Backend:**
  - Java 21 (LTS)
  - Spring Boot 3.2.5
  - Spring Web (RESTful APIs)
  - Spring Security (Stateless JWT token authentication, BCrypt password hashing, RBAC)
  - Spring Data JPA & Hibernate
  - Jakarta Bean Validation

- **Database:**
  - MySQL 8 Relational Database (`database/schema.sql` included)
  - Embedded H2 database configuration option (`application-dev.properties`) for instant local testing without setting up MySQL beforehand.

---

## 2. THE SIX CORE WORKSPACE MODULES

1. **Dashboard & Command Cockpit:**
   - Real-time statistics queried live from MySQL: Total Screenplays, Saved Ideas, Active Productions, and Cinematic Studies.
   - Recently edited screenplay drafts and recent vault ideas.
   - Quick studio navigation pills.

2. **Screenwriting Studio:**
   - **Story Development:** Loglines, story concepts, premises, themes, and central dramatic conflicts.
   - **Screenplay Structures:** Deep support for Five industry structures:
     - Three-Act Structure (Setup, Rising Conflict, Climax)
     - Five-Act Dramatic Structure (Exposition, Rising Action, Climax, Falling Action, Resolution)
     - Hero's Journey (Monomyth)
     - Save the Cat Beat Sheet
     - Non-linear Storytelling Structure
   - **Character Development:** Character names, roles (Protagonist, Antagonist, Mentor, Supporting), backstories, motivations, goals, flaws, internal/external conflicts, and arcs.
   - **Scene Breakdown:** Scene number, sluglines (`INT./EXT.`, Location, Time of Day), character appearances, objective, emotional purpose, action beats, and dialogue.
   - **Screenplay Editor:** Industry-standard formatted script editor with automated uppercase slugline insertion, autosave, and modification timestamps.

3. **AI Mentor & Creative Dramaturg:**
   - Real conversational assistant for diagnosing narrative bottlenecks, subtext analysis, and cinematic advice.
   - Context-aware: passes connected screenplay metadata (logline, genre, central conflict) to the AI assistant.
   - Production security: API keys are read securely from system environment variables (`AI_API_KEY`) and never sent to frontend JavaScript.

4. **Production Pipeline:**
   - Manages all five phases of filmmaking:
     1. Development
     2. Pre-production (Casting, Location Scouting, Gear Rentals, Shooting Breakdown)
     3. Production (Shooting Days, Audio Sync, Footage Ingest)
     4. Post-production (Rough Cut Assembly, Sound/Foley, Color Grading, Master Export)
     5. Completed
   - Real-time percentage progress calculated automatically from completed stage tasks.

5. **Cinematic Library:**
   - A dedicated film study and technique notebook.
   - Categorised by: Cinematography, Editing Techniques, Lighting Schemes, Sound Design, Shot Composition, Director's Notes, and Filmmaking Lessons.
   - Real-time full-text search across titles, film references, observations, and tags.

6. **Creative Vault:**
   - Preserves unvarnished ideas: Raw stories, one-liners, loglines, plot twists, scene inspirations, and dialogue snippets.
   - Star favourites and filter by category.
   - **One-Click Screenplay Conversion:** Instantly spawns a new Screenplay Project directly from any preserved vault idea.

---

## 3. PROJECT FOLDER STRUCTURE

```
zero-to-hundreds-hunt/
│
├── frontend/
│   ├── index.html                   # Professional cinematic landing page
│   ├── pages/
│   │   ├── login.html               # Authentication login page
│   │   ├── signup.html              # Registration with password strength meter
│   │   ├── forgot-password.html     # Secure token-based password recovery
│   │   └── dashboard.html           # Full workspace containing all 6 modules
│   ├── css/
│   │   ├── styles.css               # Film noir design system & variables
│   │   ├── auth.css                 # Auth layouts & password meter styling
│   │   └── dashboard.css            # Workspace layouts, editor, & Kanban styling
│   └── js/
│       ├── api.js                   # REST API client & auth session manager
│       ├── auth.js                  # Login, registration, password reset logic
│       └── dashboard.js             # SPA module routing & full CRUD logic
│
├── backend/
│   ├── pom.xml                      # Maven configuration with Java 21 & Spring Boot
│   ├── mvnw.cmd                     # Maven wrapper script
│   └── src/
│       ├── main/
│       │   ├── java/com/zerohundreds/hunt/
│       │   │   ├── HuntApplication.java
│       │   │   ├── config/          # Spring Security & CORS configuration
│       │   │   ├── controller/      # REST API endpoints for all modules
│       │   │   ├── service/         # Business logic layer
│       │   │   ├── repository/      # Spring Data JPA repositories
│       │   │   ├── entity/          # Relational database entity definitions
│       │   │   ├── dto/             # Request/Response Data Transfer Objects
│       │   │   ├── security/        # JWT filter, token provider, & user details
│       │   │   └── exception/       # Global exception handler & error responses
│       │   └── resources/
│       │       ├── application.properties      # MySQL 8 production database config
│       │       └── application-dev.properties  # Standalone testing config
│
├── database/
│   └── schema.sql                   # MySQL 8 DDL schema script with FK constraints
│
└── README.md
```

---

## 4. LOCAL EXECUTION & SETUP INSTRUCTIONS

### Step 1: Database Setup (MySQL 8)
1. Open MySQL Workbench or MySQL Shell:
   ```bash
   mysql -u root -p
   ```
2. Execute the schema script:
   ```sql
   source d:/Antigravity projects/zero-to-hundreds-hunt/database/schema.sql;
   ```
3. Update `backend/src/main/resources/application.properties` with your MySQL password if different from `root`.

### Step 2: Running the Spring Boot Backend
Open the `backend/` folder in VS Code, IntelliJ IDEA, or a terminal:
```bash
cd "d:\Antigravity projects\zero-to-hundreds-hunt\backend"
mvn clean spring-boot:run
```
*(Or in IntelliJ / Eclipse: run `HuntApplication.java` directly as a Spring Boot application).*

The server will launch at: `http://localhost:8080`.
You can verify the backend health status at `http://localhost:8080/api/health`.

### Step 3: Running the Frontend
The frontend is built with pure standards-based HTML5, CSS3, and JavaScript:
1. You can serve it using VS Code **Live Server** extension, or any static HTTP server (e.g. `npx serve frontend` or `python -m http.server 5500`).
2. Alternatively, open `frontend/index.html` in your browser.

---

## 5. API DOCUMENTATION SAMPLES

### Authentication Endpoints
- `POST /api/auth/register` - Registers a new filmmaker account (returns JWT).
- `POST /api/auth/login` - Authenticates user and returns JWT.
- `GET /api/auth/me` - Retrieves profile of currently authenticated user.
- `POST /api/auth/forgot-password` - Generates single-use reset token.
- `POST /api/auth/reset-password` - Resets password with valid token.
- `POST /api/auth/change-password` - Updates password after verifying current password.

### Workspace Endpoints (Protected with `Bearer <token>`)
- `GET /api/dashboard/summary` - Live counts and recent activity for the user.
- `GET, POST /api/screenplays` - Screenplay projects CRUD.
- `GET, PUT /api/screenplays/{id}/structures/{stageId}` - Structure beat notes.
- `GET, POST, PUT, DELETE /api/screenplays/{id}/characters` - Character arcs.
- `GET, POST, PUT, DELETE /api/screenplays/{id}/scenes` - Scene breakdown.
- `GET, PUT /api/screenplays/{id}/draft` - Screenplay script editor draft.
- `POST /api/ai-mentor/chat` - AI Dramaturg chat.
- `GET, POST, PUT, DELETE /api/production/projects` - Production pipeline projects.
- `GET, POST, PUT, DELETE /api/production/projects/{id}/tasks` - Pipeline tasks.
- `GET, POST, PUT, DELETE /api/library` - Film analysis notebook entries.
- `GET, POST, PUT, DELETE /api/vault` - Creative ideas & convert to screenplay.
