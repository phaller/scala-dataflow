package scala.dataflow






package object api {
  
  type Id[X] = X
  
  def task[U](body: =>U) {
  }
  
  def select[T](flows: FlowLike[T]*): FlowReader[T] = {
    null
  }
  
  case class WithdrawCallbackException() extends util.control.ControlThrowable
  
  def withdraw: Nothing = throw new WithdrawCallbackException

  def future[T](x: T) = {
    val f = new Future[T]()
    f.complete(x)
    f
  }
  
}


package api {
  
  trait FlowLike[T] {
    
    def reader: FlowReader[T]
    
    def onBind[U](body: T => U): Unit
    
    def barrier: this.type
    
  }
  
  trait FlowMapFactory[Flow[X, Y, Z[_]]] {
    
    type Blocking[K, V] = Flow[K, V, Id]
    
  }
  
  trait FlowFactory[Flow[X, Z[_]]] {
    
    type Blocking[T] = Flow[T, Id]
    
  }

    
}
