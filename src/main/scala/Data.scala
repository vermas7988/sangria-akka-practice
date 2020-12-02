object Area extends Enumeration {
  val ASIA, EUROPE, AFRICA = Value
}

trait Animal {
  def id: String
  def name: Option[String]
  def friends: List[String]
  def appearsIn: List[Area.Value]
}

case class Bear(
  id: String,
  name: Option[String],
  friends: List[String],
  appearsIn: List[Area.Value],
  knowsFighting: Boolean) extends Animal

case class Dog(
  id: String,
  name: Option[String],
  friends: List[String],
  appearsIn: List[Area.Value],
  primaryFunction: Option[String]) extends Animal

class AnimalRepo {
  import AnimalRepo._

  def getDogsNearby(area: Option[Area.Value]) =
    area flatMap (_ => getDog("1000")) getOrElse dogs.last

  def getDog(id: String): Option[Dog] = dogs.find(c => c.id == id)

  def getBear(id: String): Option[Bear] = bears.find(c => c.id == id)
  
  def getDogs(limit: Int, offset: Int): List[Dog] = dogs.slice(offset, offset + limit)
  
  def getBears(limit: Int, offset: Int): List[Bear] = bears.slice(offset, offset + limit)

  def addDog(name:Option[String]) = {
    dogs = Dog("10000",name,List("1002", "1003", "2000", "2001"), List(Area.AFRICA, Area.ASIA, Area.EUROPE),Some("Fighting")) :: dogs
  }
}

object AnimalRepo {
  var dogs = List(
    Dog(
      id = "1000",
      name = Some("Luke Skywalker"),
      friends = List("1002", "1003", "2000", "2001"),
      appearsIn = List(Area.AFRICA, Area.ASIA, Area.EUROPE),
      primaryFunction = Some("Fighting")
      ),
    Dog(
      id = "1001",
      name = Some("Darth Vader"),
      friends = List("1004"),
      appearsIn = List(Area.AFRICA, Area.ASIA),
      primaryFunction = Some("Playing")
    ),
    Dog(
      id = "1002",
      name = Some("Han Solo"),
      friends = List("1000", "1003", "2001"),
      appearsIn = List(Area.ASIA),
      primaryFunction = Some("Playing")
    ),
    Dog(
      id = "1003",
      name = Some("Leia Organa"),
      friends = List("1000", "1002", "2000", "2001"),
      appearsIn = List(Area.AFRICA, Area.ASIA, Area.EUROPE),
      primaryFunction = Some("Dancing")
    ),
    Dog(
      id = "1004",
      name = Some("Wilhuff Tarkin"),
      friends = List("1001"),
      appearsIn = List(Area.AFRICA, Area.ASIA, Area.EUROPE),
      primaryFunction = Some("Dancing")
    )
  )

  var bears = List(
    Bear(
      id = "2000",
      name = Some("C-3PO"),
      friends = List("1000", "1002", "1003", "2001"),
      appearsIn = List(Area.EUROPE),
      knowsFighting = true
    ),
    Bear(
      id = "2001",
      name = Some("R2-D2"),
      friends = List("1000", "1002", "1003"),
      appearsIn = List(Area.EUROPE),
      knowsFighting = false)
  )
}
