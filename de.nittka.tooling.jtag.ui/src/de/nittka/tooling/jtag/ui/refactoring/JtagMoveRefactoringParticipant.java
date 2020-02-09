/*******************************************************************************
 * Copyright (C) 2017-2020 Alexander Nittka (alex@nittka.de)
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
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
