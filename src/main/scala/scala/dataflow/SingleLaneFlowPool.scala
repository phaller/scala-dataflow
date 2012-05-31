package scala.dataflow



import scala.annotation.tailrec
import jsr166y._



class SingleLaneFlowPool[T] extends FlowPool[T] {

  import FlowPool._
  
  val initBlock = newBlock(0)
  
  def newPool[S] = new SingleLaneFlowPool[S]()

  def builder = new SingleLaneBuilder[T](initBlock)
  
  def aggregate[S](zero: S)(cmb: (S, S) => S)(folder: (S, T) => S): Future[S] = {
    val fut = new Future[S]()
    val cbe = new CallbackElem[T, S](folder, (sz, acc) => fut complete acc, CallbackNil, initBlock, 0, zero)
    task(new RegisterCallbackTask(cbe))
    fut
  }
  
  // def doForAll[U](f: T => U): Future[Int] = {
  //   val fut = new Future[Int]()
  //   val cbe = new CallbackElem(f, fut.complete _, CallbackNil, initBlock, 0)
  //   task(new RegisterCallbackTask(cbe))
  //   fut
  // }

  // def mapFold[U, V >: U](accInit: V)(cmb: (V,V) => V)(map: T => U): Future[(Int, V)] = {
  //   /* We do not need to synchronize on this var, because IN THE
  //    * CURRENT SETTING, callbacks are only executed in sequence.
  //    * This WILL break if the scheduling changes
  //    */
  //   @volatile var acc = accInit

  //   doForAll { x =>
  //     acc = cmb(map(x), acc)
  //   } map {
  //     c => (c,acc)
  //   }
  // }

}