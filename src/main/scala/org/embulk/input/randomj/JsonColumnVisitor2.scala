package org.embulk.input.randomj

import java.util.Map
import java.util.Random

import com.fasterxml.jackson.databind.JsonNode
import org.apache.commons.text.{CharacterPredicates, RandomStringGenerator}

import org.embulk.input.randomj.SupportedJsonObject._

class JsonColumnVisitor2(var map: Map[String, Any]) {
  final private val rnd = new Random
  final private val generator = new RandomStringGenerator.Builder()
    .withinRange('0', 'z')
    .filteredBy(CharacterPredicates.LETTERS, CharacterPredicates.DIGITS)
    .build

  final private val ITEMS = "items"

  def booleanNode(node: JsonNode): Unit = {
    val key = node.get("name").asText
    Math.random match {
      case n if n < 0.5 => map.put(key, true)
      case _ => map.put(key,  false)
    }
  }

  def doubleNode(node: JsonNode): Unit = {
    val key = node.get("name").asText
    map.put(key, rnd.nextDouble * 10000)
  }

  def integerNode(node: JsonNode): Unit = {
    val key = node.get("name").asText
    map.put(key, rnd.nextInt(10000))
  }

  def stringNode(node: JsonNode): Unit = {
    val key = node.get("name").asText
    map.put(key, generator.generate(8))
  }

//  def arrayNode(node: JsonNode): Unit = { // NOSONAR
//    val key = node.get("name").asText
//    val dataType = node.get(JsonColumnVisitor2.ITEMS).get("type").asText
//    val arraySize = node.get(JsonColumnVisitor2.ITEMS).get("size").asInt(1)
//    val jtype = SupportedJsonObject.valueOf(dataType.toUpperCase)
//    jtype match {
//      case BOOLEAN =>
//        val m = new util.ArrayList[Boolean]
//        var i = 0
//        while ( {
//          i < arraySize
//        }) {
//          if (Math.random < 0.5) m.add(true)
//          else m.add(false) {
//            i += 1;
//            i - 1
//          }
//        }
//        map.put(key, m)
//      case INTEGER =>
//        val m = new util.ArrayList[Integer]
//        var i = 0
//        while ( {
//          i < arraySize
//        }) {
//          m.add(rnd.nextInt(100)) {
//            i += 1;
//            i - 1
//          }
//        }
//        map.put(key, m)
//      case NUMBER =>
//        val m = new util.ArrayList[Number]
//        var i = 0
//        while ( {
//          i < arraySize
//        }) {
//          m.add(rnd.nextDouble * 100) {
//            i += 1;
//            i - 1
//          }
//        }
//        map.put(key, m)
//      case STRING =>
//        val length = 8
//        val m = new util.ArrayList[String]
//        var i = 0
//        while ( {
//          i < arraySize
//        }) {
//          m.add(generator.generate(length)) {
//            i += 1;
//            i - 1
//          }
//        }
//        map.put(key, m)
//      case _ =>
//        throw new UnsupportedOperationException("randomj input plugin does not support json-array-data type")
//    }
//  }
//
//  def objectNode(node: JsonNode): Unit = {
//    val objectMap = new util.HashMap[String, AnyRef]
//    import scala.collection.JavaConversions._
//    for (jsonNode <- node.findValues(JsonColumnVisitor2.ITEMS).listIterator.next) {
//      val nestKey = jsonNode.get("name").asText
//      val jtype = SupportedJsonObject.valueOf(jsonNode.get("type").asText.toUpperCase)
//      jtype match {
//        case BOOLEAN =>
//          if (Math.random < 0.5) objectMap.put(nestKey, true)
//          else objectMap.put(nestKey, false)
//        case NUMBER =>
//          objectMap.put(nestKey, rnd.nextDouble * 100)
//        case INTEGER =>
//          objectMap.put(nestKey, rnd.nextInt(10000))
//        case STRING =>
//          val length = 8
//          objectMap.put(nestKey, generator.generate(length))
//        case _ =>
//          throw new UnsupportedOperationException("randomj input plugin does not support json-data type")
//      }
//    }
//    map.put(node.get("name").asText, objectMap)
//  }
}