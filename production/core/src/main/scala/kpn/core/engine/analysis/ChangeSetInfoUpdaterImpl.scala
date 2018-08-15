package kpn.core.engine.analysis

import kpn.core.repository.ChangeSetInfoRepository
import kpn.core.repository.TaskRepository
import kpn.core.util.IdCache

class ChangeSetInfoUpdaterImpl(
  changeSetInfoRepository: ChangeSetInfoRepository,
  taskRepository: TaskRepository
) extends ChangeSetInfoUpdater {

  private val cache = new IdCache()

  override def changeSetInfo(changeSetId: Long): Unit = {

    if (cache.contains(changeSetId)) {
      // the changeset info is already in the database
    }
    else {
      cache.put(changeSetId)
      if (changeSetInfoRepository.exists(changeSetId)) {
        // the changeset info is already in the database
      }
      else {
        val taskId = TaskRepository.changeSetInfoTask + changeSetId
        if (taskRepository.exists(taskId)) {
          // there already is a request registered to fetch the changeset info from OSM API
        }
        else {
          taskRepository.add(TaskRepository.changeSetInfoTask + changeSetId)
        }
      }
    }
  }
}
