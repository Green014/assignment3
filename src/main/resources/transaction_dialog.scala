<?xml version="1.0" encoding="UTF-8"?>

<?import javafx.scene.control.Button?>
<?import javafx.scene.control.Label?>
<?import javafx.scene.control.TextField?>
<?import javafx.scene.layout.HBox?>
<?import javafx.scene.layout.VBox?>

<VBox alignment="CENTER" prefHeight="200.0" prefWidth="300.0" spacing="10.0" style="-fx-background-color: #f5e8d8; -fx-border-color: #a48c6b; -fx-border-width: 5; -fx-background-radius: 10; -fx-border-radius: 10;" xmlns="http://javafx.com/javafx/21" xmlns:fx="http://javafx.com/fxml/1" fx:controller="TransactionController">
    <children>
        <Label fx:id="itemLabel" text="Item Name" style="-fx-font-size: 16; -fx-font-weight: bold;"/>
        <HBox alignment="CENTER" spacing="10.0">
            <children>
                <Label text="Quantity:" />
                <TextField fx:id="quantityField" prefWidth="50.0" />
            </children>
        </HBox>
        <HBox alignment="CENTER" spacing="20.0">
            <children>
                <Button fx:id="okButton" mnemonicParsing="false" text="OK" style="-fx-background-color: #d1b18e; -fx-border-color: #a48c6b;" />
                <Button fx:id="cancelButton" mnemonicParsing="false" text="Cancel" style="-fx-background-color: #d1b18e; -fx-border-color: #a48c6b;" />
            </children>
        </HBox>
    </children>
</VBox>
