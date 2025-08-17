// src/main/scala/Main.scala
import scalafx.application.JFXApp3
import scalafx.scene.Scene
import javafx.fxml.FXMLLoader
import java.net.URL
import scalafx.Includes._


object Main extends JFXApp3 {
  override def start(): Unit = {
    try {
      val fxmlLoader = new FXMLLoader()
      val fxmlUrl: URL = getClass.getResource("/main.fxml")
      if (fxmlUrl == null) {
        throw new RuntimeException("Cannot find /main.fxml resource.")
      }
      fxmlLoader.setLocation(fxmlUrl)
      val root = fxmlLoader.load[javafx.scene.Parent]

      val gameModel = new GameModel()
      val imageManager = new ImageManager()
      val gameController = fxmlLoader.getController[GameController]()

      gameController.setGameModel(gameModel)
      gameController.setImageManager(imageManager)
      gameController.initData()

      stage = new JFXApp3.PrimaryStage {
        title = "Farm Game"
        scene = new Scene(root)
        resizable = false
      }
    } catch {
      case e: Exception =>
        e.printStackTrace()
    }
  }
}
