package com.ant.myagile.utils;

import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

import org.apache.log4j.Logger;

public class LogPhaseListener implements PhaseListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public long startTime;

	private static final Logger LOG = Logger.getLogger(LogPhaseListener.class);
	
	@Override
	public void afterPhase(PhaseEvent event) {
		if (event.getPhaseId() == PhaseId.RENDER_RESPONSE) {
			long endTime = System.nanoTime();
			long diffMs = (long) ((endTime - startTime) * 0.000001);
			if (LOG.isDebugEnabled()) {
				LOG.debug("Execution Time = " + diffMs + "ms");
			}
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("Executed Phase " + event.getPhaseId());
		}
	}
	
	@Override
	public void beforePhase(PhaseEvent event) {

		if (event.getPhaseId() == PhaseId.RESTORE_VIEW) {
			startTime = System.nanoTime();
		}
	}

	public PhaseId getPhaseId() {
		return PhaseId.ANY_PHASE;
	}

}
