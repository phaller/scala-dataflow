package scala.dataflow



import annotation.tailrec



final class FlowLatch[T](initial: T)(val aggregator: (T, T) => T) {
  import FlowLatch._
  
  @volatile
  protected var value: Value[T] = new Unsealed(0, initial)
  val future = new Future[T]()
  
  @inline
  private def CAS(ov: Value[T], nv: Value[T]) = unsafe.compareAndSwapObject(this, VALUE_OFFSET, ov, nv)
  
  @tailrec
  def <<(x: T) {
    val ov = /*READ*/value
    val nv = ov.add(x, aggregator)
    if (CAS(ov, nv)) checkComplete(nv)
    else <<(x)
  }
  
  @tailrec
  def seal(sz: Int): this.type = {
    val ov = /*READ*/value
    val nv = ov.seal(sz)
    if (CAS(ov, nv)) {
      checkComplete(nv)
      this
    } else seal(sz)
  }
  
  private def checkComplete(nv: Value[T]) = nv match {
    case s: Sealed[T] if s.total == s.max => future complete nv.value
    case _ =>
  }
  
}


object FlowLatch {
  
  val unsafe = getUnsafe()
  val VALUE_OFFSET = unsafe.objectFieldOffset(classOf[FlowLatch[_]].getDeclaredField("value"))
  
  def apply[T](initial: T)(aggr: (T, T) => T) = new FlowLatch(initial)(aggr)
  
  trait Value[T] {
    val total: Int
    val value: T
    def add(x: T, op: (T, T) => T): Value[T]
    def seal(size: Int): Value[T]
  }
  
  class Unsealed[T](val total: Int, val value: T) extends Value[T] {
    def add(x: T, op: (T, T) => T) =
      new Unsealed(total + 1, op(value, x))
    def seal(size: Int) =
      new Sealed(total, size, value)
  }
  
  class Sealed[T](val total: Int, val max: Int, val value: T) extends Value[T] {
    def add(x: T, op: (T, T) => T) =
      if (total < max) new Sealed(total + 1, max, op(value, x))
      else sys.error("sealed at %d".format(max))
    def seal(size: Int) =
      if (size != max) sys.error("already sealed") else this
  }
  
}
