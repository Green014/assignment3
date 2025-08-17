// src/main/scala/HistoryController.scala
import javafx.fxml.FXML
import javafx.scene.control.{Button, TextArea}
import javafx.stage.Stage
import scalafx.scene.control.{Button as SfxButton, TextArea as SfxTextArea}
import scalafx.scene.layout.VBox as SfxVBox
import java.util.stream.Collectors
import scala.collection.JavaConverters._


class HistoryController {

  @FXML var historyTextArea: TextArea = _
  @FXML var closeButton: Button = _

  var dialogStage: Stage = _
  private var gameModel: GameModel = _
  def setGameModel(model: GameModel): Unit = {
    this.gameModel = model
  }
  private var imageManager: ImageManager = _
  def setImageManager(manager: ImageManager): Unit = {
    this.imageManager = manager
  }
  var parentController: GameController = _
  
  def initData(): Unit = {
    new SfxTextArea(historyTextArea).text = gameModel.messageHistory.asJava.stream().collect(Collectors.joining("\n"))
  }

  @FXML
  def initialize(): Unit = {
    new SfxButton(closeButton).onAction = _ => {
      if (dialogStage != null) {
        dialogStage.close()
      }
    }
  }
}
