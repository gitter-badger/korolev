import korolev.{BrowserEffects, KorolevServer, Shtml, StateStorage}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
  */
object Example extends BrowserEffects[Future, State] with App with Shtml {

  import korolev.EventResult._

  KorolevServer[State](
    port = 8181,
    stateStorage = StateStorage.default(State()),
    render = {

      // Handler to input
      val inputId = elementId

      // Create a DOM using state
      { case state =>
        'body(
          'div("Super TODO tracker"),
          'div('style /= "height: 250px; overflow-y: scroll",
            (state.todos zipWithIndex) map {
              case (todo, i) =>
                'div(
                  'input(
                    'type /= "checkbox",
                    'checked when todo.done,
                    // Generate transition when clicking checkboxes
                    event('click) {
                      immediateTransition { case tState =>
                        val updated = tState.todos.updated(i, tState.todos(i).copy(done = !todo.done))
                        tState.copy(todos = updated)
                      }
                    }

                  ),
                  if (!todo.done) 'span(todo.text)
                  else 'strike(todo.text)
                )
            }
          ),
          'form(
            // Generate AddTodo action when 'Add' button clicked
            eventWithAccess('submit) { access =>
              deferredTransition {
                access.property[String](inputId, 'value) map { value =>
                  val todo = Todo(value, done = false)
                  transition { case tState =>
                    tState.copy(todos = tState.todos :+ todo)
                  }
                }
              }
            },
            'input(
              inputId,
              'type /= "text",
              'placeholder /= "What should be done?"
            ),
            'button("Add todo")
          )
        )
      }
    }
  )
}

case class Todo(text: String, done: Boolean)

case class State(todos: Vector[Todo] = (0 to 2).toVector map {
  i => Todo(s"This is TODO #$i", done = false)
})

