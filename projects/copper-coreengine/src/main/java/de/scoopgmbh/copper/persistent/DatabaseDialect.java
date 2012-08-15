package de.scoopgmbh.copper.persistent;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import de.scoopgmbh.copper.Response;
import de.scoopgmbh.copper.Workflow;
import de.scoopgmbh.copper.batcher.BatchCommand;

public interface DatabaseDialect {

	public abstract void resumeBrokenBusinessProcesses(Connection con) throws Exception;

	public abstract List<Workflow<?>> dequeue(final String ppoolId,	final int max, Connection con) throws Exception;

	public abstract int updateQueueState(final int max, final Connection con) throws SQLException;

	public abstract int deleteStaleResponse(Connection con, int maxRows) throws Exception;

	public abstract void insert(final List<Workflow<?>> wfs, final Connection con) throws Exception;

	public abstract void insert(final Workflow<?> wf, final Connection con) throws Exception;

	public abstract void restart(final String workflowInstanceId, Connection c) throws Exception;

	public abstract void restartAll(Connection c) throws Exception;

	public abstract void notify(List<Response<?>> responses, Connection c) throws Exception;

	@SuppressWarnings({"rawtypes"})
	public abstract BatchCommand createBatchCommand4Finish(final Workflow<?> w);

	@SuppressWarnings({"rawtypes"})
	public abstract BatchCommand createBatchCommand4Notify(final Response<?> response) throws Exception;

	@SuppressWarnings({"rawtypes"})
	public abstract BatchCommand createBatchCommand4registerCallback(final RegisterCall rc, final ScottyDBStorageInterface dbStorageInterface) throws Exception;

	@SuppressWarnings({"rawtypes"})
	public abstract BatchCommand createBatchCommand4error(Workflow<?> w, Throwable t);
	
	/**
	 * If true (default), finished workflow instances are removed from the database.
	 */
	public void setRemoveWhenFinished(boolean removeWhenFinished);	

}