package de.tudresden.inf.lat.gentlerepair.plugin

import org.protege.editor.owl.ui.OWLWorkspaceViewsTab

@SerialVersionUID(2364494254783378893L)
class GentleRepairTab extends OWLWorkspaceViewsTab {

  this.setToolTipText("Custom tooltip text for Gentle Repair Tab")

  override def initialise(): Unit = {
    super.initialise()
    // log.info("Gentle Repair Tab initialized")
  }

  override def dispose(): Unit = {
    super.dispose()
    // log.info("Gentle Repair Tab disposed")
  }

}

