package de.nittka.tooling.jtag.ui.refactoring;

import javax.inject.Inject;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.MoveParticipant;

public class JtagMoveRefactoringParticipant extends MoveParticipant {

	@Inject
	private JtagRefactoringPreventer delegate=new JtagRefactoringPreventer();

	@Override
	protected boolean initialize(Object object) {
		delegate.setResourceToMove(object);
		return true;
	}

	@Override
	public Change createChange(IProgressMonitor arg0) throws CoreException, OperationCanceledException {
		return null;
	}

	@Override
	public String getName() {
		return JtagRefactoringPreventer.NAME;
	}

	@Override
	public RefactoringStatus checkConditions(IProgressMonitor arg0, CheckConditionsContext arg1)
			throws OperationCanceledException {
		return delegate.checkConditions("moving");
	}
}
