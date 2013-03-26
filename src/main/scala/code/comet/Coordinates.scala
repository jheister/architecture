package code.comet

case class Coordinates(x: Int, y: Int) {
  def minus(diff: Coordinates) = Coordinates(x - diff.x, y - diff.y)

  def plus(sum: Coordinates) = Coordinates(x + sum.x, y + sum.y)

  def dividedBy(div: Int) = Coordinates(x / div, y / div)
}
