// src/main/scala/GameController.scala
import javafx.animation.{KeyFrame, Timeline}
import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.fxml.{FXML, FXMLLoader}
import javafx.scene.control.{Button, Label}
import javafx.scene.image.ImageView
import javafx.scene.input.MouseEvent
import javafx.scene.layout.{AnchorPane, GridPane, StackPane}
import javafx.scene.{Parent, Scene}
import javafx.stage.Stage
import scalafx.Includes.*
import scalafx.scene.control.{Button as SfxButton, Label as SfxLabel}
import scalafx.scene.effect.GaussianBlur as SfxGaussianBlur
import scalafx.scene.image.ImageView as SfxImageView
import scalafx.scene.layout.{AnchorPane as SfxAnchorPane, GridPane as SfxGridPane, StackPane as SfxStackPane}

class GameController {

  @FXML var dayLabel: Label = _
  @FXML var moneyLabel: Label = _
  @FXML var inventoryButton: Button = _
  @FXML var shopButton: Button = _
  @FXML var restButton: Button = _
  @FXML var hoeButton: Button = _
  @FXML var seedButton: Button = _
  @FXML var waterButton: Button = _
  @FXML var harvestButton: Button = _
  @FXML var historyButton: Button = _
  @FXML var objectiveLabel: Label = _
  @FXML var endGameButton: Button = _

  @FXML var farmGrid: GridPane = _

  @FXML var messageContainer: StackPane = _
  @FXML var toastLabel: Label = _

  @FXML var mainRoot: AnchorPane = _
  @FXML var gameContentPane: AnchorPane = _
  @FXML var backgroundImageView: ImageView = _
  @FXML var endGameOverlay: AnchorPane = _
  @FXML var endGameTitleLabel: Label = _
  @FXML var endGameMessageLabel: Label = _
  @FXML var endGameScoreLabel: Label = _
  @FXML var endGameQuitButton: Button = _

  private var inventoryStage: Stage = _
  var currentMode: String = "Normal"
  var selectedSeed: String = ""

  private var gameModel: GameModel = _
  def setGameModel(model: GameModel): Unit = {
    this.gameModel = model
  }

  private var imageManager: ImageManager = _
  def setImageManager(manager: ImageManager): Unit = {
    this.imageManager = manager
  }

  def initData(): Unit = {
    new SfxLabel(moneyLabel).text <== Bindings.createStringBinding(
      () => s"Money: ${gameModel.money.value}", gameModel.money)
    new SfxLabel(dayLabel).text <== Bindings.createStringBinding(
      () => s"Day ${gameModel.day.value}", gameModel.day)
    val objectiveText = gameModel.objective.map { case (item, count) => s"${count} ${item}" }.mkString(", ")
    objectiveLabel.setText(s"Objective: Collect ${objectiveText} by Day ${gameModel.gameEndDay}")

    for (y <- 0 until gameModel.mapHeight; x <- 0 until gameModel.mapWidth) {
      val imageView = new SfxImageView(imageManager.grassImage)
      imageView.fitWidth = 80
      imageView.fitHeight = 80
      imageView.onMouseClicked = (event: MouseEvent) => {
        handleFarmClick(x, y)
      }
      new SfxGridPane(farmGrid).add(imageView, x, y)
    }

    new SfxImageView(backgroundImageView).image = imageManager.mainBackgroundImage
  }

  @FXML
  def initialize(): Unit = {
    new SfxStackPane(messageContainer).opacity = 0.0

    new SfxButton(hoeButton).onAction = _ => {
      currentMode = "Hoeing"
      updateStatus("Current mode set to: Hoeing")
    }

    new SfxButton(seedButton).onAction = _ => {
      showInventory("seeds")
    }

    new SfxButton(inventoryButton).onAction = _ => {
      showInventory("all")
    }

    new SfxButton(waterButton).onAction = _ => {
      currentMode = "Watering"
      updateStatus("Current mode set to: Watering")
    }

    new SfxButton(restButton).onAction = _ => {
      gameModel.day.value += 1
      updateStatus(s"You rested for the night. Current day is Day ${gameModel.day.value}.")
      gameModel.growCrops()
      updateFarmView()
      if (gameModel.day.value >= gameModel.gameEndDay) {
        endGame()
      }
    }

    new SfxButton(harvestButton).onAction = _ => {
      currentMode = "Harvesting"
      updateStatus("Current mode set to: Harvesting")
    }

    new SfxButton(shopButton).onAction = _ => {
      showShop()
    }

    new SfxButton(historyButton).onAction = _ => {
      showHistoryWindow()
    }

    new SfxButton(endGameButton).onAction = _ => {
      endGame()
    }

    new SfxAnchorPane(endGameOverlay).visible = false
    new SfxButton(endGameQuitButton).onAction = _ => Platform.exit()
  }

  private def updateFarmView(): Unit = {
    for (y <- 0 until gameModel.mapHeight; x <- 0 until gameModel.mapWidth) {
      updateFarmTileImage(x, y)
    }
  }

  private def endGame(): Unit = {
    val (win, extraScore) = gameModel.checkVictoryCondition()

    new SfxAnchorPane(endGameOverlay).visible = true
    new SfxAnchorPane(gameContentPane).effect = new SfxGaussianBlur(10.0)

    if (win) {
      new SfxLabel(endGameTitleLabel).text = "Victory!"
      endGameTitleLabel.setStyle("-fx-text-fill: #24a13d;")
      new SfxLabel(endGameMessageLabel).text = "Congratulations! You have achieved your objective."
      new SfxLabel(endGameScoreLabel).text = s"Extra Score: $extraScore Gold"
    } else {
      new SfxLabel(endGameTitleLabel).text = "Game Over!"
      endGameTitleLabel.setStyle("-fx-text-fill: red;")
      new SfxLabel(endGameMessageLabel).text = "Unfortunately, you did not achieve your objective in time."
      new SfxLabel(endGameScoreLabel).text = ""
    }
  }

  private def updateStatus(message: String): Unit = {
    gameModel.messageHistory.append(message)
    if (gameModel.messageHistory.length > 50) {
      gameModel.messageHistory.remove(0, 1)
    }

    new SfxLabel(toastLabel).text = message
    new SfxStackPane(messageContainer).opacity = 1.0

    val timeline = new Timeline(
      new KeyFrame(javafx.util.Duration.seconds(3), _ => {
        new SfxStackPane(messageContainer).opacity = 0.0
      })
    )
    timeline.play()
  }

  private def showHistoryWindow(): Unit = {
    try {
      val fxmlUrl = getClass.getResource("/history.fxml")
      val loader = new FXMLLoader(fxmlUrl)
      val root: Parent = loader.load()

      val historyController = loader.getController[HistoryController]()

      val stage = new Stage()
      stage.setTitle("Game History")
      stage.setScene(new Scene(root))

      historyController.dialogStage = stage
      historyController.setGameModel(gameModel)
      historyController.setImageManager(imageManager)
      historyController.initData()

      stage.show()
    } catch {
      case e: Exception => e.printStackTrace()
    }
  }

  private def showShop(): Unit = {
    try {
      val fxmlUrl = getClass.getResource("/shop.fxml")
      val loader = new FXMLLoader(fxmlUrl)
      val root: Parent = loader.load()

      val shopController = loader.getController[ShopController]()

      val stage = new Stage()
      stage.setTitle("Shop")
      stage.setScene(new Scene(root))

      shopController.dialogStage = stage
      shopController.setGameModel(gameModel)
      shopController.setImageManager(imageManager)
      shopController.parentController = this
      shopController.initData()

      stage.show()
    } catch {
      case e: Exception => e.printStackTrace()
    }
  }

  def showInventory(category: String): Unit = {
    if (inventoryStage != null && inventoryStage.isShowing) {
      inventoryStage.toFront()
      val inventoryController = inventoryStage.getScene.getRoot.getUserData.asInstanceOf[InventoryController]
      inventoryController.currentCategory = category
      inventoryController.renderInventory()
    } else {
      try {
        val fxmlUrl = getClass.getResource("/inventory.fxml")
        val loader = new FXMLLoader(fxmlUrl)
        val root: Parent = loader.load()

        val inventoryController = loader.getController[InventoryController]()

        inventoryStage = new Stage()
        inventoryStage.setTitle("Inventory")
        inventoryStage.setScene(new Scene(root))

        root.setUserData(inventoryController)

        inventoryController.dialogStage = inventoryStage
        inventoryController.setGameModel(gameModel)
        inventoryController.setImageManager(imageManager)
        inventoryController.parentController = this
        inventoryController.initData()

        inventoryStage.show()
      } catch {
        case e: Exception => e.printStackTrace()
      }
    }
  }

  private def updateFarmTileImage(x: Int, y: Int): Unit = {
    val farmTile = gameModel.farmMap(y)(x).value
    val imageView = new SfxImageView(farmGrid.getChildren.get(y * gameModel.mapWidth + x).asInstanceOf[ImageView])

    farmTile.state.value match {
      case Empty =>
        imageView.image = imageManager.grassImage
      case Hoeing =>
        imageView.image = imageManager.hoeingImage
      case RequireWater =>
        imageView.image = imageManager.carrotRequireWaterImage
      case Dead =>
        imageView.image = imageManager.deadImage
      case _ =>
        farmTile.crop.value match {
          case Some(crop) =>
            crop.growthStage match {
              case Seedling =>
                imageView.image = imageManager.carrotSeedlingImage
              case Growing =>
                imageView.image = imageManager.carrotGrowingImage
              case Mature =>
                imageView.image = imageManager.carrotMatureImage
            }
          case None =>
        }
    }
  }

  private def handleFarmClick(x: Int, y: Int): Unit = {
    val farmTile = gameModel.farmMap(y)(x).value
    currentMode match {
      case "Hoeing" =>
        if (farmTile.state.value == Empty || farmTile.state.value == Dead) {
          if (gameModel.performHoe(x, y)) {
            updateFarmView()
            updateStatus(s"Grid ($x, $y) has been hoed.")
          }
        }
        currentMode = "Normal"

      case "Seeding" =>
        if (selectedSeed.nonEmpty && farmTile.state.value == Hoeing) {
          val seedCount = Option(gameModel.inventory.get(selectedSeed)).map(_.intValue()).getOrElse(0)
          if (seedCount > 0) {
            gameModel.performSeeding(x, y, selectedSeed)
            updateFarmView()
            val cropName = gameModel.cropProperties.get(selectedSeed).get("name").asInstanceOf[String]
            updateStatus(s"Grid ($x, y) planted with ${cropName}, remaining: ${gameModel.inventory.get(selectedSeed)}")
          }
        }
        currentMode = "Normal"
        selectedSeed = ""

      case "Watering" =>
        if (farmTile.state.value == RequireWater) {
          if (gameModel.performWatering(x, y)) {
            updateFarmView()
            updateStatus(s"Crop in grid ($x, y) has been watered.")
          }
        } else {
          updateStatus(s"This grid does not require watering.")
        }
        currentMode = "Normal"

      case "Harvesting" =>
        if (farmTile.state.value == Grown) {
          val harvestedCropName = farmTile.crop.value.get.name
          if (gameModel.performHarvest(x, y)) {
            updateFarmView()
            updateStatus(s"Harvested ${harvestedCropName}, added 1 to inventory.")
          }
        } else {
          updateStatus(s"The crop in this grid is not yet mature.")
        }
        currentMode = "Normal"

      case _ =>
        updateStatus(s"Current mode is Normal, unable to perform action.")
    }
  }
}
