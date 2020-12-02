import monocle.Monocle.toApplyFoldOps
import sangria.execution.deferred.{Fetcher, HasId}
import sangria.macros.derive
import sangria.macros.derive.InputObjectTypeName
import sangria.schema._

import scala.concurrent.Future

/**
 * Defines a GraphQL schema for the current project
 */
object SchemaDefinition {

  val animals = Fetcher.caching(
    (ctx: AnimalRepo, ids: Seq[String]) =>
      Future.successful(ids.flatMap(id => ctx.getDog(id) orElse ctx.getBear(id))))(HasId(_.id))

  val AreaEnum = EnumType(
    "Area",
    Some("Areas in which animal can be found"),
    List(
      EnumValue("EUROPE",
        value = Area.EUROPE,
        description = Some("Weather is cold")),
      EnumValue("ASIA",
        value = Area.ASIA,
        description = Some("Mixed weather,")),
      EnumValue("AFRICA",
        value = Area.AFRICA,
        description = Some("Hot weather"))))

  val Animal: InterfaceType[AnimalRepo, Animal] =
    InterfaceType(
      "Animal",
      "A animal",
      () => fields[AnimalRepo, Animal](
        Field("id", StringType,
          Some("The id of the character."),
          resolve = _.value.id),
        Field("name", OptionType(StringType),
          Some("The name of the character."),
          resolve = _.value.name),
        Field("friends", ListType(Animal),
          Some("The friends of the Animal, or an empty list if they have none."),
          resolve = ctx => animals.deferSeqOpt(ctx.value.friends)),
        Field("appearsIn", OptionType(ListType(OptionType(AreaEnum))),
          Some("Which area they appear in."),
          resolve = _.value.appearsIn map (e => Some(e)))
      ))

  val Bear =
    ObjectType(
      "Bear",
      "A bear.",
      interfaces[AnimalRepo, Bear](Animal),
      fields[AnimalRepo, Bear](
        Field("id", StringType,
          Some("The id of the bear."),
          resolve = _.value.id),
        Field("name", OptionType(StringType),
          Some("The name of the bear."),
          resolve = _.value.name),
        Field("friends", ListType(Animal),
          Some("The friends of the bear, or an empty list if they have none."),
          resolve = ctx => animals.deferSeqOpt(ctx.value.friends)),
        Field("appearsIn", OptionType(ListType(OptionType(AreaEnum))),
          Some("Which areas they appear in."),
          resolve = _.value.appearsIn map (e => Some(e))),
        Field("knowsFighting", OptionType(BooleanType),
          Some("if he knows o fight."),
          resolve = _.value.knowsFighting   ),
      ))

  val Dog = ObjectType(
    "Dog",
    "A dog.",
    interfaces[AnimalRepo, Dog](Animal),
    fields[AnimalRepo, Dog](
      Field("id", StringType,
        Some("The id of the dog."),
        resolve = _.value.id),
      Field("name", OptionType(StringType),
        Some("The name of the dog."),
        resolve = ctx => Future.successful(ctx.value.name)),
      Field("friends", ListType(Animal),
        Some("The friends of the dog, or an empty list if they have none."),
        resolve = ctx => animals.deferSeqOpt(ctx.value.friends)),
      Field("appearsIn", OptionType(ListType(OptionType(AreaEnum))),
        Some("Which area they appear in."),
        resolve = _.value.appearsIn map (e => Some(e))),
      Field("primaryFunction", OptionType(StringType),
        Some("The primary function of the dog."),
        resolve = _.value.primaryFunction)
    ))

  val ID = Argument("id", StringType, description = "id of the animal")

  val AreaArg = Argument("area", OptionInputType(AreaEnum),
    description = "If omitted, returns the main dog of the whole universe. If provided, returns the hero of that particular area.")

  val LimitArg = Argument("limit", OptionInputType(IntType), defaultValue = 20)
  val OffsetArg = Argument("offset", OptionInputType(IntType), defaultValue = 0)

  val Query = ObjectType(
    "Query", fields[AnimalRepo, Unit](
      Field("areaMain", Animal,
        arguments = AreaArg :: Nil,
        resolve = (ctx) => ctx.ctx.getDogsNearby(ctx.arg(AreaArg))),
      Field("dog", OptionType(Dog),
        arguments = ID :: Nil,
        resolve = ctx => ctx.ctx.getDog(ctx arg ID)),
      Field("bear", Bear,
        arguments = ID :: Nil,
        resolve = ctx => ctx.ctx.getBear(ctx arg ID).get),
      Field("dogs", ListType(Dog),
        arguments = LimitArg :: OffsetArg :: Nil,
        resolve = ctx => ctx.ctx.getDogs(ctx arg LimitArg, ctx arg OffsetArg)),
      Field("bears", ListType(Bear),
        arguments = LimitArg :: OffsetArg :: Nil,
        resolve = ctx => ctx.ctx.getBears(ctx arg LimitArg, ctx arg OffsetArg))
    ))


  val NameArg = Argument("name",OptionInputType(StringType),"name of the dog.")

  val Mutation = ObjectType(
    "Mutation",
    fields[AnimalRepo, Unit](
      Field("addDog",
        Dog,
        arguments = ID :: Nil,
        resolve = c => c.ctx.addDog(c arg NameArg)
      )
    )
  )

  val AnimalSagaSchema = Schema(Query, Some(Mutation))
}
