package models.braks

import models.mongo.MongoBrak
import models.mongo.MongoUser
import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO


class FamilyTree(var value: Any) {
    var left: FamilyTree? = null
    var right: FamilyTree? = null

    fun build(): String{
        val sb = StringBuilder()
        sb.append(this.value)
        val pointerRight = "└──"
        val pointerLeft = if (right != null) "├──" else "└──"
        buildTreeRecursive(sb, "", pointerLeft, left, right != null)
        buildTreeRecursive(sb, "", pointerRight, right, false)
        return sb.toString()
    }

    private fun buildTreeRecursive(sb: StringBuilder, padding: String?, pointer: String?, node: FamilyTree?, hasRightSibling: Boolean) {
        if (node != null) {
            sb.append("\n")
            sb.append(padding)
            sb.append(pointer)
            sb.append(node.value)
            val paddingBuilder = StringBuilder(padding)
            if (hasRightSibling) {
                paddingBuilder.append("│  ")
            } else {
                paddingBuilder.append("   ")
            }
            val paddingForBoth = paddingBuilder.toString()
            val pointerRight = "└──"
            val pointerLeft = if (node.right != null) "├──" else "└──"
            buildTreeRecursive(sb, paddingForBoth, pointerLeft, node.left, node.right != null)
            buildTreeRecursive(sb, paddingForBoth, pointerRight, node.right, false)
        }
    }

    fun toImage(): ByteArray{
        val list = this.build().split("\n")
        val maxlength = list.stream().max(Comparator.comparingInt(String::length)).get().length

        val widthOffset = 40
        val imageWidth = widthOffset + maxlength*12

        val heightOffset = 80
        val imageHeight = heightOffset+21*list.size

        val bufferedImage = BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB)
        val graphics: Graphics2D = bufferedImage.createGraphics()

        graphics.color = Color.black
        graphics.fillRect(0, 0, imageWidth, imageHeight)

        graphics.color = Color.white
        graphics.font = Font("Arial", Font.PLAIN, 20)

        val lineHeight = graphics.fontMetrics.height
        var y = lineHeight + heightOffset/4

        for (line in list) {
            graphics.drawString(line, widthOffset/2, y)
            y += lineHeight
        }

        graphics.dispose()

        val byteArrayOutputStream = ByteArrayOutputStream()
        try {
            ImageIO.write(bufferedImage, "png", byteArrayOutputStream)
        } catch (e: Exception) {
            println("Ошибка при создании изображения: ${e.message}")
        }

        return byteArrayOutputStream.toByteArray()
    }

    companion object{
        suspend fun create(userID: Long): FamilyTree {
            if(MongoBrak.getFromUserID(userID) == null) return FamilyTree("У тебя нет брака :c")
            val tree = FamilyTree(userID)
            createTreeRecursive(userID, tree)
            return tree
        }

        private suspend fun createTreeRecursive(userID: Long, tree: FamilyTree) {
            val brak = MongoBrak.getFromUserID(userID) ?: return
            val partnerID = if (brak.firstUserID == userID) brak.secondUserID else brak.firstUserID
            val partner = MongoUser.getFromUserID(partnerID?:-1L)
            tree.value = MongoUser.getFromUserID(userID)?.username ?: "null"
            tree.left = FamilyTree(partner?.username ?: "null")
            if (brak.baby != null) {
                val child = brak.baby!!.user()
                tree.right = FamilyTree(child?.username ?: "not fnd")
                createTreeRecursive(child?.id ?: -1, tree.right!!)
            }
        }

    }

}