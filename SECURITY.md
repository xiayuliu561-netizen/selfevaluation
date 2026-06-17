# Security Notes

- Do not commit `resources/connection.properties`; use `resources/connection.properties.example` as the template.
- Change the default `admin / 123456` account after first deployment.
- Keep AI provider API keys only in the application database or an external secret manager.
- Runtime uploads and logs under `runtime/` may contain personal or operational data and must not be published.
- The current legacy user table stores passwords as plain text in SQL samples. Treat this as a known technical debt and migrate to salted password hashing before production use.
