db = connect('mongodb://mongo_user:mongo_password@crackhash_db_mongo:27017/myapp?authSource=admin');

print("Running migrations...");

db.crack_hash_tasks.insertOne({
    _id: "user-alice-001",
    name: "Alice",
    email: "alice@example.com"
})

print("Migrations finished.");