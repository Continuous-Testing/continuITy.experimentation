package org.continuity.experimentation.builder;

import java.util.function.BooleanSupplier;

import org.continuity.experimentation.Experiment;
import org.continuity.experimentation.IExperimentAction;
import org.continuity.experimentation.IExperimentElement;
import org.continuity.experimentation.element.NamedBooleanSupplier;

/**
 * @author Henning Schulz
 *
 */
public class ExperimentBuilderImpl extends AbstractExperimentBuilder<Experiment> implements StableExperimentBuilder {

	private String experimentName;

	public ExperimentBuilderImpl(String name) {
		super(null, null);

		this.experimentName = name;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public StableExperimentBuilder append(IExperimentAction action) {
		appendAction(action);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public LoopBuilder<StableExperimentBuilder> loop(int numIterations) {
		return new LoopBuilderImpl<>(this, this::appendElement, numIterations);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IfBranchBuilder<StableExperimentBuilder> ifThen(BooleanSupplier condition) {
		return ifThen("?", condition);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IfBranchBuilder<StableExperimentBuilder> ifThen(String name, BooleanSupplier condition) {
		return new BranchBuilderImpl<>(this, this::appendElement, new NamedBooleanSupplier(name, condition));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ConcurrentBuilder<StableExperimentBuilder> newThread() {
		return new ConcurrentBuilderImpl<>(this, this::appendElement);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Experiment build() {
		getCurrent().setNextOrFail(IExperimentElement.END);
		Experiment experiment = new Experiment(experimentName, getFirst());

		for (IExperimentElement element : experiment) {
			if (element.hasAction()) {
				element.getAction().bypassExperiment(experiment);
			}
		}

		return experiment;
	}

}
