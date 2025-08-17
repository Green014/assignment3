// src/main/scala/ShopController.scala
import javafx.animation.{KeyFrame, Timeline}
import javafx.beans.binding.Bindings
import javafx.fxml.{FXML, FXMLLoader}
import javafx.scene.{Parent, Scene}
import javafx.scene.control.{Button, Label}
import javafx.scene.image.ImageView
import javafx.scene.input.MouseEvent
import javafx.scene.layout.{GridPane, StackPane}
import javafx.stage.Stage
import javafx.util.Duration
import scalafx.Includes.*
import scalafx.scene.control.{Button as SfxButton, Label as SfxLabel}
import scalafx.scene.image.ImageView as SfxImageView
import scalafx.scene.layout.{GridPane as SfxGridPane, StackPane as SfxStackPane, VBox as SfxVBox}

import scala.collection.JavaConverters.*

class ShopController {

  @FXML var shopkeeperImageView: ImageView = _
  @FXML var moneyLabel: Label = _
  @FXML var buyButton: Button = _
  @FXML var sellButton: Button = _
  @FXML var shopGrid: GridPane = _
  @FXML var closeButton: Button = _
  @FXML var dialogueContainer: StackPane = _
  @FXML var dialogueLabel: Label = _

  var dialogStage: Stage = _
  private var gameModel: GameModel = _
  def setGameModel(model: GameModel): Unit = {
    this.gameModel = model
  }
  var parentController: GameController = _
  private var imageManager: ImageManager = _
  def setImageManager(manager: ImageManager): Unit = {
    this.imageManager = manager
  }

  var currentView: String = "buy"

  def initData(): Unit = {
    new SfxLabel(moneyLabel).text <== Bindings.createStringBinding(
      () => s"Money: ${gameModel.money.value}", gameModel.money)

    // Set initial dialogue
    updateDialogue("Welcome! What would you like to trade?")

    // Set initial view
    currentView = "buy"
    renderShop()

    new SfxImageView(shopkeeperImageView).image = imageManager.shopkeeperImage
  }

  @FXML
  def initialize(): Unit = {
    new SfxButton(closeButton).onAction = _ => dialogStage.close()

    new SfxButton(buyButton).onAction = _ => {
      currentView = "buy"
      renderShop()
      updateDialogue("You have a good eye.")
    }
    new SfxButton(sellButton).onAction = _ => {
      currentView = "sell"
      renderShop()
      updateDialogue("Let me see what you have brought.")
    }
  }

  def renderShop(): Unit = {
    new SfxGridPane(shopGrid).children.clear()

    val itemsToShow = currentView match {
      case "buy" => gameModel.cropProperties.toSeq
      case "sell" => gameModel.inventory.asScala.toSeq.filter { case (key, value) => !key.contains("_seed") && value > 0 }
      case _ => Seq()
    }

    val gridWidth = 3
    var index = 0

    itemsToShow.foreach { case (itemName, itemData) =>
      val row = index / gridWidth
      val col = index % gridWidth

      val itemSlot = createShopSlot(itemName, itemData)

      new SfxStackPane(itemSlot).onMouseClicked = (event: MouseEvent) => {
        showTransactionDialog(itemName, currentView)
      }

      new SfxGridPane(shopGrid).add(itemSlot, col, row)
      index += 1
    }
  }

  private def createShopSlot(itemName: String, itemData: Any): StackPane = {
    val (image, name, price) = itemData match {
      case data: Map[String, Any] =>
        val name = data("name").asInstanceOf[String]
        val price = data("sell_price").asInstanceOf[Int]
        val img = if (itemName.contains("_seed")) {
          imageManager.carrotSeedBagImage
        } else {
          imageManager.carrotCropImage
        }
        (img, name, price)
      case _ =>
        val name = itemName
        val price = gameModel.cropProperties.values.find(v => v.get("harvest_name").contains(itemName)).flatMap(_.get("sell_price")).map(_.asInstanceOf[Int]).getOrElse(0)
        val img = imageManager.carrotCropImage
        (img, name, price)
    }

    val itemImageView = new SfxImageView(image)
    itemImageView.fitWidth = 80
    itemImageView.fitHeight = 80

    val nameLabel = new SfxLabel(name)
    val priceLabel = new SfxLabel(s"Price: $price")

    val slotVBox = new SfxVBox(nameLabel, itemImageView, priceLabel)
    slotVBox.spacing = 5.0
    slotVBox.alignment = javafx.geometry.Pos.CENTER

    val slotPane = new StackPane()
    slotPane.getChildren.add(slotVBox.delegate)
    new SfxStackPane(slotPane).prefWidth = 100
    new SfxStackPane(slotPane).prefHeight = 150
    new SfxStackPane(slotPane).style = "-fx-background-color: #e0d8c0; -fx-border-color: #a48c6b; -fx-border-width: 1;"

    slotPane
  }

  def updateDialogue(message: String): Unit = {
    new SfxLabel(dialogueLabel).text = message
    new SfxStackPane(dialogueContainer).opacity = 1.0

    val timeline = new Timeline(
      new KeyFrame(Duration.seconds(4), _ => {
        new SfxStackPane(dialogueContainer).opacity = 0.0
      })
    )
    timeline.play()
  }

  private def showTransactionDialog(itemName: String, view: String): Unit = {
    try {
      val fxmlUrl = getClass.getResource("/transaction_dialog.fxml")
      val loader = new FXMLLoader(fxmlUrl)
      val root: Parent = loader.load()

      val transactionController = loader.getController[TransactionController]()

      val stage = new Stage()
      stage.setTitle(s"${if (view == "buy") "Buy" else "Sell"}")
      stage.setScene(new Scene(root))

      transactionController.dialogStage = stage
      transactionController.setGameModel(gameModel)
      transactionController.setShopController(this)
      transactionController.initialize(itemName, view)

      stage.show()
    } catch {
      case e: Exception => e.printStackTrace()
    }
  }

  def performBuy(itemName: String, quantity: Int): Unit = {
    if (gameModel.performBuy(itemName, quantity)) {
      updateDialogue("Thank you for your business.")
      renderShop()
    } else {
      updateDialogue("Insufficient funds.")
    }
  }

  def performSell(itemName: String, quantity: Int): Unit = {
    if (gameModel.performSell(itemName, quantity)) {
      updateDialogue("Thank you for your business.")
      renderShop()
    } else {
      updateDialogue("Insufficient stock.")
    }
  }
}
