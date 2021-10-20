package education.pratice.calcuoator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.room.Room
import education.pratice.calcuoator.model.History
import org.w3c.dom.Text
import java.lang.NumberFormatException

class MainActivity : AppCompatActivity() {

    private val expressionTextView: TextView by lazy {
        findViewById<TextView>(R.id.expressionTextView)
    }

    private val resultTextView: TextView by lazy {
        findViewById<TextView>(R.id.resultTextView)
    }

    private var isOperator = false
    private var hasOperator = false


    private val historyLayout : View by lazy {
        findViewById<View>(R.id.historyLayout)
    }

    private val historyLinearLayout : LinearLayout by lazy {
        findViewById<LinearLayout>(R.id.historyLinearLayout)
    }


    lateinit var db:AppDataBase //늦은 초기화. 초기화 값을 나중에 할당한다.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = Room.databaseBuilder( //db에 값을 할당한다. 빌더를 반환한다.
            applicationContext, //context
            AppDataBase::class.java, //class 이름
            "historyDB" //실제로 db가 저장될 때 사용되는 이름
        ).build() //onCreate가 될 때 appDataBase가 만들어져 생성이 된다.
    }


    fun buttonClicked(v: View) {

        when(v.id) {
            R.id.button0 -> numberButtonClicked("0")
            R.id.button1 -> numberButtonClicked("1")
            R.id.button2 -> numberButtonClicked("2")
            R.id.button3 -> numberButtonClicked("3")
            R.id.button4 -> numberButtonClicked("4")
            R.id.button5 -> numberButtonClicked("5")
            R.id.button6 -> numberButtonClicked("6")
            R.id.button7 -> numberButtonClicked("7")
            R.id.button8 -> numberButtonClicked("8")
            R.id.button9 -> numberButtonClicked("9")

            R.id.buttonPlus -> operatorButtonClicked("+")
            R.id.buttonMinus -> operatorButtonClicked("-")
            R.id.buttonX -> operatorButtonClicked("*")
            R.id.buttonDivider -> operatorButtonClicked("/")
            R.id.buttonModulo -> operatorButtonClicked("%")

        }

    }

    private fun numberButtonClicked(number : String) {

        if (isOperator) {
            expressionTextView.append(" ")
        }

        isOperator = false

        val expressionText = expressionTextView.text.split(" ")

        if (expressionText.isNotEmpty() && expressionText.last().length >= 15) { //사용자가 입력한 수식이 비지 않았고 15자리 이상일 경우
            Toast.makeText(this, "15자리까지만 사용할 수 있습니다.",Toast.LENGTH_SHORT).show()
            return
        } else if (expressionText.last().isEmpty() && number == "0") {
            Toast.makeText(this,"0은 제일 앞에 올 수 없습니다.",Toast.LENGTH_SHORT).show()
            return
        }
        // 예외처리
        expressionTextView.append(number)
        resultTextView.text = calculateExpression() //결과값 호출

    }

    private fun operatorButtonClicked(operator : String) { //string 값을 받아서 처리한다(연산자)

        if (expressionTextView.text.isEmpty()) {
            return
        }

        //이미 연산자가 있는데 연산자를 또 누른 경우
        when {
            isOperator -> {
                val text = expressionTextView.text.toString() //입력된 값 받아오기
                expressionTextView.text = text.dropLast(1) + operator //마지막 1자리 없애기
            }

            hasOperator -> { //연산자가 연속으로 입력되었을 경우ㅂ
                Toast.makeText(this, "연산자는 한 번만 사용할 수 있습니다.", Toast.LENGTH_SHORT).show()
                return
            }

            else -> { //is. has 전부 false인 경우
                expressionTextView.append(" $operator") //숫자만 입력하고 연산자가 없는 경우
            }
        }
        val ssb = SpannableStringBuilder(expressionTextView.text) //연산자의 색상만 변경해 정보 저장
        ssb.setSpan( //setSapn 통해 text 속성 할당
            ForegroundColorSpan(getColor(R.color.green)), //reen을
            expressionTextView.text.length -1, //연산자에만
            expressionTextView.text.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE //적용한다.
        )

        expressionTextView.text = ssb

        hasOperator = true
        isOperator = true

    }

    fun clearButtonClicked(view: android.view.View) { //초기화
        hasOperator = false
        isOperator = false
        expressionTextView.text = ""
        resultTextView.text = ""
    }


    fun resultbuttonClicked(view: android.view.View) {
        val expressionText = expressionTextView.text.split(" ") //빈칸이 발견되면 문자를 잘라 저장한다.

        if (expressionTextView.text.isEmpty() || expressionText.size == 1) {
            return
        } //비어있는 경우, 숫자밖에 없는 경우(연산자도 입력되지 않은 경우)

        if (expressionText.size != 3 && hasOperator) {
            //숫자와 연산자만 입력하고 두 번째 숫자를 입력하지 않은 경우
            Toast.makeText(this,"아직 완성되지 않은 수식입니다.", Toast.LENGTH_SHORT).show()
        }

        if (expressionText[0].isNumber().not() || expressionText[2].isNumber().not()) {
            Toast.makeText(this," 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
        }

        val expressionText2 = expressionTextView.text.toString()
        val resultText = calculateExpression()

        //todo 디비에 넣어주는 부분

        Thread(Runnable {  //어느 스레드가 먼저 실행될 지 알 수 없다.
            db.HistoryDao().insertHistory(History(null, expressionText2, resultText)) //dao 안에 history 하나를 넣어준다.
        }).start() //스레드 스타트


        resultTextView.text = ""
        expressionTextView.text = resultText

        isOperator = false
        hasOperator = false



    }

    private fun calculateExpression() : String {
        val expressionText = expressionTextView.text.split(" ") //빈칸이 발견되면 문자를 잘라 저장한다.

        if (hasOperator.not() || expressionText.size != 3) { //왼숫자 연산자 오른숫자 = 3
            return ""
        } else if (expressionText[0].isNumber().not() || expressionText[2].isNumber().not()) {
            return "" //왼숫자가 숫자가 아니거나, 오른숫자가 숫자가 아닐때 예외처리
        }

        val exp1 = expressionText[0].toBigInteger() //연산자 왼쪽 숫자
        val exp2 = expressionText[2].toBigInteger() //연산자 오른쪽 숫자
        val op = expressionText[1] //연산자

        return when(op) {
            "+" -> (exp1 + exp2).toString()
            "-" -> (exp1 - exp2).toString()
            "*" -> (exp1 * exp2).toString()
            "%" -> (exp1 % exp2).toString()
            "/" -> (exp1 / exp2).toString()
            else -> ""
        }

    }

    //객체 확장 함수 만들기 : 객체.확장함수이름
    fun String.isNumber(): Boolean {
        return try {
            this.toBigInteger()
            true
        } catch (e: NumberFormatException){
            false
        }
    }

    fun historybuttonClicked(view: android.view.View) {
        historyLayout.isVisible = true
        historyLinearLayout.removeAllViews()


        //TODO 디비에서 모든 기록 가져오기
        //TODO 뷰에 모든 기록 할당하기


        Thread(Runnable {

            db.HistoryDao().getAll().reversed().forEach { //먼저 넣은 것을 위에 표시하기 위해 리스트를 뒤집는다.
                runOnUiThread { //ui스레드를 실행시킨다.
                    val historyView = LayoutInflater.from(this).inflate(R.layout.history_row, null, false) //inflater를 만들어 할당한다. 이때 연결 화면은 hitroy_row 이다.
                    historyView.findViewById<TextView>(R.id.expressionTextView).text = it.expression //
                    historyView.findViewById<TextView>(R.id.resultTextView).text = "= ${it.result}"

                    historyLinearLayout.addView(historyView)

                }
            }

        }).start()


    }
    fun HistoryClearButtonClicked(view: android.view.View) {

        //TODO 디비에서 모든 기록 삭제
        //TODO 뷰에서 모든 기록 삭제

        historyLinearLayout.removeAllViews() //뷰들이 전부 지워진다.
        Thread(Runnable { //db의 메소드를 불러오는 법 : 메소드를 열고,

            db.HistoryDao().deleteAll() //dao를 가져와 메소드를 불러온다.

        }).start() //스레드 실행 시키기

    }
    fun CloseHistoryButtonClicked(view: android.view.View) {

        historyLayout.isVisible = false

    }


}