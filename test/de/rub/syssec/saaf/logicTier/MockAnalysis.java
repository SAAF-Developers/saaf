/**
 * 
 */
package de.rub.syssec.saaf.logicTier;

import java.io.File;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import de.rub.syssec.saaf.model.SAAFException;
import de.rub.syssec.saaf.model.analysis.AnalysisException;
import de.rub.syssec.saaf.model.analysis.AnalysisInterface;
import de.rub.syssec.saaf.model.analysis.BTResultInterface;
import de.rub.syssec.saaf.model.analysis.HResultInterface;
import de.rub.syssec.saaf.model.application.ApplicationInterface;

/**
 * @author Tilman Bender <tilman.bender@rub.de>
 *
 */
public class MockAnalysis implements AnalysisInterface {

	private int id;
	private int hvalue;
	private ApplicationInterface app;
	private boolean changed=true;
	private Status status=Status.FINISHED;

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.saaf.db.Entity#getId()
	 */
	@Override
	public int getId() {
		return this.id;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.saaf.db.Entity#setId(int)
	 */
	@Override
	public void setId(int id) {
		this.id = id;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.saaf.logicTier.AnalysisInterface#setHeuristicValue(int)
	 */
	@Override
	public void setHeuristicValue(int heuristicValue) {
		this.hvalue = heuristicValue;
		setChanged(true);
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.saaf.logicTier.AnalysisInterface#getStatus()
	 */
	@Override
	public Status getStatus() {
		return this.status;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.saaf.logicTier.AnalysisInterface#setStatus(int)
	 */
	@Override
	public void setStatus(Status status) {
		this.status=status;
		setChanged(true);
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.saaf.logicTier.AnalysisInterface#getApp()
	 */
	@Override
	public ApplicationInterface getApp() {
		return this.app;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.saaf.logicTier.AnalysisInterface#getHeuristicValue()
	 */
	@Override
	public int getHeuristicValue() {
		return this.hvalue;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.saaf.logicTier.AnalysisInterface#getStart()
	 */
	@Override
	public Date getStartTime() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.saaf.logicTier.AnalysisInterface#getStop()
	 */
	@Override
	public Date getStopTime() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.saaf.logicTier.AnalysisInterface#getCreated()
	 */
	@Override
	public Date getCreationTime() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.saaf.logicTier.AnalysisInterface#getBTResults()
	 */
	@Override
	public LinkedList<BTResultInterface> getBTResults() {
		// TODO Auto-generated method stub
		return new LinkedList<BTResultInterface>();
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.saaf.logicTier.AnalysisInterface#getHResults()
	 */
	@Override
	public LinkedList<HResultInterface> getHResults() {
		return new LinkedList<HResultInterface>();
	}

	public String getStopString() {
		return null;
	}

	public String getStartString() {
		return null;
	}

	public String getCreationTimeAsMySqlString() {
		return null;
	}

	public List<SAAFException> getNonCriticalExceptions() {
		return null;
	}

	@Override
	public void setApp(ApplicationInterface app) {
		this.app=app;	
		this.setChanged(true);
	}

	@Override
	public void setBTResults(List<BTResultInterface> btResults) {
		setChanged(true);
	}

	@Override
	public void setHResults(List<HResultInterface> heuristicResults) {
		setChanged(true);
	}

	@Override
	public void setChanged(boolean changed) {
		this.changed= changed;
		
	}

	@Override
	public boolean isChanged() {
		return this.changed;
	}

	@Override
	public void setNonCriticalExceptions(List<SAAFException> backTrackExceptions) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<SAAFException> getCriticalExceptions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setCriticalExceptions(List<SAAFException> criticalExceptions) {
		// TODO Auto-generated method stub
	}

	public void setReportFile(File report) {
	}

	public File getReportFile() {
		return null;
	}

	public void doPreprocessing() throws AnalysisException {
	}

	public void doAnalysis() throws AnalysisException {
	}

	public void doCleanUp() throws AnalysisException {
	}

	public void doGenerateReport() throws AnalysisException {
	}

}
