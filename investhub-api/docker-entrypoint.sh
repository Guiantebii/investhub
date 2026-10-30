#!/bin/sh
set -e

# Render Postgres connectionString: postgresql://user:pass@host:5432/db
if [ -n "$INVESTHUB_DB_URL" ]; then
	case "$INVESTHUB_DB_URL" in
	jdbc:*)
		export SPRING_DATASOURCE_URL="$INVESTHUB_DB_URL"
		;;
	postgres*)
		export SPRING_DATASOURCE_URL="jdbc:$INVESTHUB_DB_URL"
		;;
	esac
fi

if [ -n "$INVESTHUB_DB_USERNAME" ]; then
	export SPRING_DATASOURCE_USERNAME="$INVESTHUB_DB_USERNAME"
fi

if [ -n "$INVESTHUB_DB_PASSWORD" ]; then
	export SPRING_DATASOURCE_PASSWORD="$INVESTHUB_DB_PASSWORD"
fi

exec java $JAVA_OPTS -jar /app/app.jar
