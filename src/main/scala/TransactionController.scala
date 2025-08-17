// src/main/scala/TransactionController.scala
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.{Button, Label, TextField}
import javafx.stage.Stage
import scalafx.scene.control.{Button as SfxButton, Label as SfxLabel, TextField as SfxTextField}

class TransactionController {
  @FXML var itemLabel: Label = _
  @FXML var quantityField: TextField = _
  @FXML var okButton: Button = _
  @FXML var cancelButton: Button = _

  var dialogStage: Stage = _
  var parentController: ShopController = _
  private var gameModel: GameModel = _
  def setGameModel(model: GameModel): Unit = {
    this.gameModel = model
  }
  def setShopController(controller: ShopController): Unit = {
    this.parentController = controller
  }
  var itemName: String = _
  var view: String = _

  def initialize(item: String, currentView: String): Unit = {
    itemName = item
    view = currentView
    new SfxLabel(itemLabel).text = s"Trade: ${itemName}"

    new SfxButton(okButton).onAction = _ => handleOk()
    new SfxButton(cancelButton).onAction = _ => dialogStage.close()
  }

  private def handleOk(): Unit = {
    val quantity = try {
      new SfxTextField(quantityField).text.value.toInt
    } catch {
      case _: NumberFormatException => 0
    }

    if (quantity > 0) {
      if (view == "buy") {
        parentController.performBuy(itemName, quantity)
      } else {
        parentController.performSell(itemName, quantity)
      }
    }
    dialogStage.close()
  }
}
