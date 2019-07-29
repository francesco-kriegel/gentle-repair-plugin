package de.tudresden.inf.lat.gentlerepair.plugin;

import org.protege.editor.owl.ui.OWLWorkspaceViewsTab;

public class GentleRepairTab extends OWLWorkspaceViewsTab {

  private static final long serialVersionUID = 2364494254783378893L;
//  private static final Logger log              = LoggerFactory.getLogger(GentleRepairTab.class);

  public GentleRepairTab() {
    setToolTipText("Custom tooltip text for Gentle Repair Tab");
  }

  @Override
  public void initialise() {
    super.initialise();
//    log.info("Gentle Repair Tab initialized");
  }

  @Override
  public void dispose() {
    super.dispose();
//    log.info("Gentle Repair Tab disposed");
  }

}
