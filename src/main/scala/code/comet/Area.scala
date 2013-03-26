package code.comet

import util.Random


case class Area(start: Coordinates, end: Coordinates) {
  def contains(cell: Cell) = cell.x > start.x && cell.y > start.y && cell.x <= end.x && cell.y <= end.y

  def count(nodes: Iterable[Cell]) = nodes.filter(contains(_)).size

  def randomPosition = {
    val bound = end.minus(start)

    start.plus(Coordinates(Random.nextInt(bound.x), Random.nextInt(bound.y)))
  }

  def quarter = {
    val middle: Coordinates = end.minus(start).dividedBy(2).plus(start)
    List(Area(start, middle),
         Area(Coordinates(middle.x, start.y), Coordinates(end.x, middle.y)),
         Area(Coordinates(start.x, middle.y), Coordinates(middle.x, end.y)),
         Area(middle, end))
  }
}
