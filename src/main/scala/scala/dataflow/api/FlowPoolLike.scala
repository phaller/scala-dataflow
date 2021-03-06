package scala.dataflow.api

trait FlowPoolLike[T] {

  def builder: FlowPoolLike.Builder[T]

  def foreach[U](f: T => U): Unit

  // TODO do we want to allow for blocking?

}

object FlowPoolLike {

  trait Builder[T] {
    def <<(x: T): this.type
    def seal: Unit
  }

}
