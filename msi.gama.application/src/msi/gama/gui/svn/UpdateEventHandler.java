/*
 * GAMA - V1.4  http://gama-platform.googlecode.com
 * 
 * (c) 2007-2011 UMI 209 UMMISCO IRD/UPMC & Partners (see below)
 * 
 * Developers :
 * 
 * - Alexis Drogoul, UMI 209 UMMISCO, IRD/UPMC (Kernel, Metamodel, GAML), 2007-2012
 * - Vo Duc An, UMI 209 UMMISCO, IRD/UPMC (SWT, multi-level architecture), 2008-2012
 * - Patrick Taillandier, UMR 6228 IDEES, CNRS/Univ. Rouen  (Batch, GeoTools & JTS), 2009-2012
 * - Beno�t Gaudou, UMR 5505 IRIT, CNRS/Univ. Toulouse 1 (Documentation, Tests), 2010-2012
 * - Phan Huy Cuong, DREAM team, Univ. Can Tho (XText-based GAML), 2012
 * - Pierrick Koch, UMI 209 UMMISCO, IRD/UPMC (XText-based GAML), 2010-2011
 * - Romain Lavaud, UMI 209 UMMISCO, IRD/UPMC (RCP environment), 2010
 * - Francois Sempe, UMI 209 UMMISCO, IRD/UPMC (EMF model, Batch), 2007-2009
 * - Edouard Amouroux, UMI 209 UMMISCO, IRD/UPMC (C++ initial porting), 2007-2008
 * - Chu Thanh Quang, UMI 209 UMMISCO, IRD/UPMC (OpenMap integration), 2007-2008
 */
package msi.gama.gui.svn;

import org.tmatesoft.svn.core.SVNCancelException;
import org.tmatesoft.svn.core.wc.*;

/*
 * This class is an implementation of ISVNEventHandler intended for processing
 * events generated by do*() methods of an SVNUpdateClient object. An instance
 * of this handler will be provided to an SVNUpdateClient. When calling, for
 * example, SVNWCClient.doUpdate(..) on some path, that method will generate an
 * event for each 'update'/'add'/'delete'/.. action it will perform upon every
 * path being updated. And this event is passed to
 * 
 * ISVNEventHandler.handleEvent(SVNEvent event, double progress)
 * 
 * to notify the handler. The event contains detailed information about the
 * path, action performed upon the path and some other.
 */
public class UpdateEventHandler implements ISVNEventHandler {

	/*
	 * progress is currently reserved for future purposes and now is always
	 * ISVNEventHandler.UNKNOWN
	 */
	@Override
	public void handleEvent(final SVNEvent event, final double progress) {
		/*
		 * Gets the current action. An action is represented by SVNEventAction.
		 * In case of an update an action can be determined via comparing
		 * SVNEvent.getAction() and SVNEventAction.UPDATE_-like constants.
		 */
		SVNEventAction action = event.getAction();
		String pathChangeType = " ";
		if ( action == SVNEventAction.UPDATE_ADD ) {
			/*
			 * the item was added
			 */
			pathChangeType = "A";
		} else if ( action == SVNEventAction.UPDATE_DELETE ) {
			/*
			 * the item was deleted
			 */
			pathChangeType = "D";
		} else if ( action == SVNEventAction.UPDATE_UPDATE ) {
			/*
			 * Find out in details what state the item is (after having been
			 * updated).
			 * 
			 * Gets the status of file/directory item contents. It is
			 * SVNStatusType who contains information on the state of an item.
			 */
			SVNStatusType contentsStatus = event.getContentsStatus();
			if ( contentsStatus == SVNStatusType.CHANGED ) {
				/*
				 * the item was modified in the repository (got the changes
				 * from the repository
				 */
				pathChangeType = "U";
			} else if ( contentsStatus == SVNStatusType.CONFLICTED ) {
				/*
				 * The file item is in a state of Conflict. That is, changes
				 * received from the repository during an update, overlap with
				 * local changes the user has in his working copy.
				 */
				pathChangeType = "C";
			} else if ( contentsStatus == SVNStatusType.MERGED ) {
				/*
				 * The file item was merGed (those changes that came from the
				 * repository did not overlap local changes and were merged
				 * into the file).
				 */
				pathChangeType = "G";
			}
		} else if ( action == SVNEventAction.UPDATE_EXTERNAL ) {
			/* for externals definitions */
			System.out.println("Fetching external item into '" + event.getFile().getAbsolutePath() +
				"'");
			System.out.println("External at revision " + event.getRevision());
			return;
		} else if ( action == SVNEventAction.UPDATE_COMPLETED ) {
			/*
			 * Updating the working copy is completed. Prints out the revision.
			 */
			System.out.println("At revision " + event.getRevision());
			return;
		} else if ( action == SVNEventAction.ADD ) {
			System.out.println("A     " + event.getURL());
			return;
		} else if ( action == SVNEventAction.DELETE ) {
			System.out.println("D     " + event.getURL());
			return;
		} else if ( action == SVNEventAction.LOCKED ) {
			System.out.println("L     " + event.getURL());
			return;
		} else if ( action == SVNEventAction.LOCK_FAILED ) {
			System.out.println("failed to lock    " + event.getURL());
			return;
		}

		/*
		 * Now getting the status of properties of an item. SVNStatusType also
		 * contains information on the properties state.
		 */
		SVNStatusType propertiesStatus = event.getPropertiesStatus();
		/*
		 * At first consider properties are normal (unchanged).
		 */
		String propertiesChangeType = " ";
		if ( propertiesStatus == SVNStatusType.CHANGED ) {
			/*
			 * Properties were updated.
			 */
			propertiesChangeType = "U";
		} else if ( propertiesStatus == SVNStatusType.CONFLICTED ) {
			/*
			 * Properties are in conflict with the repository.
			 */
			propertiesChangeType = "C";
		} else if ( propertiesStatus == SVNStatusType.MERGED ) {
			/*
			 * Properties that came from the repository were merged with the
			 * local ones.
			 */
			propertiesChangeType = "G";
		}

		/*
		 * Gets the status of the lock.
		 */
		String lockLabel = " ";
		SVNStatusType lockType = event.getLockStatus();

		if ( lockType == SVNStatusType.LOCK_UNLOCKED ) {
			/*
			 * The lock is broken by someone.
			 */
			lockLabel = "B";
		}

		System.out.println(pathChangeType + propertiesChangeType + lockLabel + "       " +
			event.getURL());
	}

	/*
	 * Should be implemented to check if the current operation is cancelled. If
	 * it is, this method should throw an SVNCancelException.
	 */
	@Override
	public void checkCancelled() throws SVNCancelException {}

}