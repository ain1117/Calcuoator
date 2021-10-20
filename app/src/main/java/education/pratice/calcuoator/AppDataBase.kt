package education.pratice.calcuoator

import androidx.room.Database
import androidx.room.RoomDatabase
import education.pratice.calcuoator.dao.HistoryDao
import education.pratice.calcuoator.model.History

@Database(entities = [History::class], version = 1) //history라는 테이블을 사용한다고 등록한다. version 마이그레이션 사용
abstract class AppDataBase : RoomDatabase() {
    abstract fun HistoryDao(): HistoryDao //AppDataBase를 생성할 때 HistoryDAO를 가져와 사용한다.
}

