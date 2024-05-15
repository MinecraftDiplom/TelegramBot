package database

import com.mongodb.MongoClientSettings
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import com.mongodb.client.model.Sorts.ascending
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import models.BotSettings
import org.bson.BsonValue
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.pojo.Convention
import org.bson.codecs.pojo.Conventions
import org.bson.codecs.pojo.PojoCodecProvider
import org.bson.conversions.Bson
import kotlin.reflect.KClass

/**
 * A generic MongoDB database handling class for Kotlin that abstracts common MongoDB operations.
 *
 * @param collectionStr Name of the MongoDB collection.
 * @param clazz KClass of the type T the collection holds.
 * @param settings Configuration settings for the MongoDB client, defaults to settings parsed from file.
 */
class MongoDatabase<T : Any>(private val collectionStr:String, private val clazz: KClass<T>, private val settings: BotSettings = BotSettings.parseFromFile()) {

    private var client: MongoClient? = null

    private var db: MongoDatabase? = null

    lateinit var collection: MongoCollection<T>

    /**
     * Returns an active MongoDB client or throws if not initialized.
     */
    fun client() : MongoClient = client ?: throw IllegalArgumentException("Client not active!")

    /**
     * Returns an active MongoDB database or throws if not initialized.
     */
    fun db() : MongoDatabase = db ?: throw IllegalArgumentException("Database not active!")

    /**
     * Saves an object in the collection. If an object with the same ID exists, it is replaced.
     *
     * @param idFilter The BSON filter to identify the object.
     * @param obj The object to save.
     * @return The ID of the upserted or inserted object.
     */
    suspend fun save(idFilter: Bson, obj:T) : BsonValue? {
        if (collection.find(idFilter).firstOrNull() != null){
            return collection.replaceOne(idFilter, obj).upsertedId
        }
        return collection.insertOne(obj).insertedId
    }

    /**
     * Inserts an object into the collection.
     *
     * @param obj The object to add.
     * @return The ID of the inserted object.
     */
    suspend fun add(obj:T) : BsonValue? {
        return collection.insertOne(obj).insertedId
    }

    /**
     * Replaces an existing object in the collection.
     *
     * @param idFilter The BSON filter to identify the object.
     * @param obj The new object to replace the old one.
     * @return The ID of the upserted object.
     */
    suspend fun update(idFilter: Bson, obj:T) : BsonValue? {
        return collection.replaceOne(idFilter, obj).upsertedId
    }

    /**
     * Deletes an object from the collection.
     *
     * @param idFilter The BSON filter to identify the object to delete.
     * @return The count of deleted objects.
     */
    suspend fun delete(idFilter: Bson) : Long {
        return collection.deleteOne(idFilter).deletedCount
    }

    /**
     * Finds an object in the collection.
     *
     * @param idFilter The BSON filter to identify the object.
     * @return The found object or null if not found.
     */
    suspend fun find(idFilter: Bson) : T? {
        return collection.find(idFilter).firstOrNull()
    }

    suspend fun top(sort: Bson = ascending("_id"), limit: Int = 10) : List<T> {
        return collection.find().sort(sort).limit(limit).toList()
    }

    suspend fun count():Long{
        return collection.countDocuments()
    }

    suspend fun exists(idFilter: Bson) : Boolean {
        return collection.find(idFilter).count() > 0
    }

    suspend fun transferDatabase(list: List<T>): MutableMap<Int, BsonValue> {
        val result = collection.insertMany(list).insertedIds
        println("Transfer Done. Expected: ${list.size}, inserted: ${result.size}")
        return result
    }

    suspend fun indexNumber(fieldNames:String){
        collection.createIndex(
            Indexes.ascending(fieldNames)
        )
    }
    suspend fun indexNumber(fieldNames:String, unique:Boolean = true){
        collection.createIndex(
            Indexes.ascending(fieldNames),
            IndexOptions().unique(unique)
        )
    }

    suspend fun indexCompound(vararg fields:String){
        require(fields.size > 1 ){ "Compound index require fields count must be more than 1" }
        collection.createIndex(Indexes.compoundIndex(Indexes.ascending(*fields)))
    }


    suspend fun indexText(fieldNames:String, unique:Boolean = false){
        collection.createIndex(
            Indexes.text(fieldNames),
            IndexOptions().unique(unique)
        )
    }

    fun initialize(vararg discClasses: KClass<*>){

        val pojoCodecRegistry = PojoCodecProvider.builder()
            .conventions((Conventions.DEFAULT_CONVENTIONS + Conventions.SET_PRIVATE_FIELDS_CONVENTION) as List<Convention>)
            .automatic(true)

        pojoCodecRegistry.register(clazz.java)
        discClasses.forEach {
            pojoCodecRegistry.register(it.java)
        }

        val codecRegistry =
            CodecRegistries.fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                CodecRegistries.fromProviders(pojoCodecRegistry.build())
            )

        val mongoSettings = MongoClientSettings.builder()
            .applyConnectionString(settings.toMongoConnection())
            .codecRegistry(codecRegistry)
            .build()

        MongoClient.create(mongoSettings).let { client ->
            db = client.getDatabase(settings.dbname)
            this.client = client
            collection = db?.getCollection(collectionStr, clazz.java) ?: throw IllegalArgumentException("Collection not found")
        }

    }
}