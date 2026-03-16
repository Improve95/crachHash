db = connect('mongodb://mongo_user:mongo_password@crackhash_db_mongo:27017/myapp?authSource=admin');
'mongodb://mongo_user:mongo_password@localhost:27017/myapp?authSource=admin'
rs.initiate({_id:'rs0',members:[{_id:0,host:'mongo-1:27017',priority:1},{_id:1,host:'mongo-2:27017',priority:0.7},{_id:2,host:'mongo-3:27017',priority:0.5}]})

rs.initiate({
    _id: "rs0",
    members: [
        { _id: 0, host: "mongo-1:27017" },
        { _id: 1, host: "mongo-2:27017" },
        { _id: 2, host: "mongo-3:27017" }
    ]
})