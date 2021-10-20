package education.pratice.calcuoator.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class History ( //생성자에다 변수를 입력하는 방식
    @PrimaryKey val uid: Int?, //unique
    @ColumnInfo(name="expression") val expression: String?,
    @ColumnInfo(name="result") val result: String?
)