package ject.wordplay

import org.scalajs.dom.document
import org.scalajs.dom.raw.HTMLInputElement
import slinky.core.*
import slinky.core.annotations.react
import slinky.core.facade.Fragment
import slinky.core.facade.ReactElement
import slinky.web.html.*

import scala.collection.immutable.ArraySeq
import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@JSImport("resources/app.css", JSImport.Default)
@js.native
object AppCSS extends js.Object

sealed trait QuizState

object QuizState {
  case object Init extends QuizState
  case object Correct extends QuizState
  case object Incorrect extends QuizState
  case object ShownAnswer extends QuizState
}

@react class App extends Component {
  case class Props(quizzes: ArraySeq[KanjiQuiz])
  case class State(quizIndex: Int, quizState: QuizState, text: String)

  private val css = AppCSS

  def initialState: State = State(0, QuizState.Init, "")

  def currentQuiz: KanjiQuiz = props.quizzes(state.quizIndex)

  def render(): ReactElement =
    div(id := "app")(
      div(id := "quiz-message", className := stateClass)(quizMessage),
      table(id := "kanji-grid")(
        tbody(
          tr(
            td(),
            td(className := "kanji-cell")(currentQuiz.top),
            td()
          ),
          tr(
            td(className := "kanji-cell")(currentQuiz.left),
            td(id := "input-cell", className := stateClass)(
              input(
                id := "input",
                className := stateClass,
                `type` := "text",
                autoFocus,
                value := state.text,
                onChange := { e =>
                  val userAnswer = e.target.asInstanceOf[HTMLInputElement].value

                  if (userAnswer.trim.isEmpty) {
                    setState(_.copy(quizState = QuizState.Init, text = ""))
                  } else if (currentQuiz.answers.contains(userAnswer)) {
                    setState(_.copy(quizState = QuizState.Correct, text = userAnswer))
                  } else {
                    setState(_.copy(quizState = QuizState.Incorrect, text = userAnswer))
                  }
                }
              )
            ),
            td(className := "kanji-cell")(currentQuiz.right)
          ),
          tr(
            td(),
            td(className := "kanji-cell")(currentQuiz.bottom),
            td()
          )
        )
      ),
      div(id := "quiz-selection")(
        div(className := "quiz-header")("クイズ"),
        Fragment(
          props.quizzes.indices.map { i =>
            button(
              className := s"quiz-number ${if (i == state.quizIndex) "selected" else ""}",
              onClick := { e =>
                document.getElementById("input").asInstanceOf[HTMLInputElement].focus()
                setState(State(i, QuizState.Init, ""))
              }
            )(
              (i + 1).toString
            )
          }*
        ),
        button(
          className := "show-answer",
          onClick := { e =>
            setState(_.copy(quizState = QuizState.ShownAnswer, text = currentQuiz.answers.head))
          }
        )("答えを見る")
      )
    )

  private def stateClass: String = state.quizState match {
    case QuizState.Init                            => ""
    case QuizState.Correct | QuizState.ShownAnswer => "correct"
    case QuizState.Incorrect                       => "incorrect"
  }

  private def quizMessage: String = state.quizState match {
    case QuizState.Init        => "空欄に入る漢字は何でしょう？"
    case QuizState.Correct     => "正解！ヽ(*´∀`)/"
    case QuizState.Incorrect   => "ブー！不正解(乂∀･)"
    case QuizState.ShownAnswer => "次は頑張ろうヽ(｡・∀・｡)ﾉ"
  }
}
