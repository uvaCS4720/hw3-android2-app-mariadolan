package edu.nd.pmcburne.hwapp.one
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

@Database(entities = [GameObject::class], version = 1)
abstract class GameDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
    companion object {
        @Volatile
        private var instance: GameDatabase? = null

        fun getInstance(context: Context): GameDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    GameDatabase::class.java,
                    "game_database"
                ).build().also { instance = it }
            }
        }
    }
}