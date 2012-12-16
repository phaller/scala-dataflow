package scala.dataflow.array

import scala.dataflow.Future

private[array] class FATransposeJob[A : ClassManifest] private (
  val src: FlatFlowArray[A],
  val dst: FlatFlowArray[A],
  val step: Int,
  val srcOffset: Int,
  val dstOffset: Int,
  start: Int,
  end: Int,
  thr: Int,
  obs: FAJob.Observer
) extends FAJob(start, end, thr, obs) {

  override protected type SubJob = FATransposeJob[A]

  @inline private def ci(i: Int) =
    (i / step) + (i % step) * step

  protected def subCopy(s: Int, e: Int) = 
    new FATransposeJob(src, dst, step, srcOffset, dstOffset, s, e, thresh, this)

  protected def doCompute() {
    for (i <- start to end) {
      val nind = ci(i - srcOffset) + dstOffset
      dst.data(nind) = src.data(i)
    }
  }

  /** this thing does not really cover any range */
  protected override def covers(from: Int, to: Int) = false

  override def destSliceJobs(from: Int, to: Int): Vector[FATransposeJob[A]] = {
    if (isSplit) {
      val (j1,j2) = subTasks
      j1.destSliceJobs(from, to) ++ j2.destSliceJobs(from, to)
    } else if (!done) {
      val myr = start to end
      val inds = (from to to).map(i => ci(i - dstOffset))
      if (inds.exists(myr.contains _)) Vector(this)
      else Vector()
    } else { Vector() }
  }

}

object FATransposeJob {

  import FAJob.JobGen

  def apply[A : ClassManifest](
    dst: FlatFlowArray[A],
    step: Int
  ) = new JobGen[A] {
    def apply(src: FlatFlowArray[A], dstOffset: Int, srcOffset: Int, length: Int) =
      // TODO have a better size threshold
      new FATransposeJob(src, dst, step, srcOffset, dstOffset, srcOffset,
                         srcOffset + length - 1, FAJob.threshold(length), null)
  }

}
