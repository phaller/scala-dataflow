package scala.dataflow






trait FlowVarLike[T, Async[X]] extends FlowLike[T] {
  
  def <<(x: T): Unit
  
  def apply()(implicit apply: Apply[T, FlowVar, Async]): Async[T]
  
  def blocking: FlowVar.Blocking[T]
  
}


trait FlowVar[T] extends FlowVarLike[T, Future]


object FlowVar extends FlowFactory[FlowVarLike] {
  
  def apply[T](): FlowVar[T] = null
  
}


