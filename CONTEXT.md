# Goal Orchestration

This context describes a bounded execution in which one Orchestrator may delegate sequential text work to a Worker to pursue a user-defined outcome.

## Language

**Goal**:
The outcome requested by the user. A Goal is immutable during its Goal Run.
_Avoid_: Task, prompt

**Goal Run**:
One bounded attempt to pursue a Goal. Only one Goal Run may be active in the MVP.
_Avoid_: Session, job

**Goal Run State**:
The lifecycle state of a Goal Run: `RUNNING`, `COMPLETED`, `FAILED`, `LIMIT_REACHED`, or `CANCELLED`.
_Avoid_: Goal Outcome, progress message

**Goal Outcome**:
The Orchestrator's final claim that the Goal was `ACHIEVED` or `NOT_ACHIEVED`, with a textual answer. Only a completed Goal Run has a Goal Outcome.
_Avoid_: Goal Run State, verified result

**Orchestrator**:
The agent responsible for deciding whether to delegate the next piece of work or finish the Goal Run.
_Avoid_: Manager, supervisor

**Orchestration Cycle**:
One opportunity for the Orchestrator to inspect the Goal and prior Worker Results, then make one Orchestrator Decision.
_Avoid_: Retry, worker call

**Orchestrator Decision**:
One structured choice made during an Orchestration Cycle: `DELEGATE` or `FINISH`.
_Avoid_: Raw model response, combined action

**Delegation**:
A textual assignment from the Orchestrator to the Worker. Delegations execute one at a time.
_Avoid_: Batch, spawn, handoff

**Worker**:
The generic agent that performs one Delegation and returns a textual Worker Result.
_Avoid_: Worker Profile, specialized agent

**Worker Run**:
One bounded execution of one Delegation. A Worker Run makes exactly one model call.
_Avoid_: Worker Cycle, child session

**Worker Result**:
The textual result returned by a successful Worker Run for the Orchestrator to consider in its next cycle.
_Avoid_: Goal Outcome, file artifact

**Execution Limits**:
The configured maximum numbers of Orchestration Cycles and Worker Runs allowed in one Goal Run.
_Avoid_: Cost guarantee, dynamic profile

**Cancellation Request**:
A user instruction that prevents a Goal Run from starting more work. An active model call may finish for usage recording, but its returned decision or result is ignored.
_Avoid_: Forced interruption, immediate cancellation
