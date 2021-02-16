# polls

FIXME: description

## Usage

### Build frontend and run the backend

    lein release

    lein run

### Open re-frame app

    open http://localhost:3000/re-frame

### Try controllers

    curl http://localhost:3000/
    Unauthorized
    
    curl http://localhost:3000/wrong
    Not Found
    
    curl -H "Authorization: Basic YWxhZGRpbjpvcGVuc2VzYW1l" http://localhost:3000/
    Index page
    
    curl -H "Authorization: Basic YWxhZGRpbjpvcGVuc2VzYW1l" http://localhost:3000/wrong
    Not Found

## Migrations

### Create migration 
Creates two files (edn and clj) in src/db
    lein migrations.create question

### Apply migrations
    lein migrations.migrate

### Rollback last migration
    lein migrations.rollback-last

### Reset all migrations
    lein migrations.reset