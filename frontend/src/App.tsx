import { useEffect, useState } from 'react'

type GoalRunState = 'RUNNING' | 'COMPLETED' | 'FAILED' | 'LIMIT_REACHED' | 'CANCELLED'
type GoalOutcome = 'ACHIEVED' | 'NOT_ACHIEVED'

type GoalRun = {
  id: string
  goal: string
  state: GoalRunState
  outcome: GoalOutcome | null
  finalAnswer: string | null
  failureReason: string | null
  createdAt: string
  updatedAt: string
}

type ApiError = {
  message?: string
}

async function readGoalRun(id: string): Promise<GoalRun> {
  const response = await fetch(`/api/goal-runs/${id}`)
  if (!response.ok) {
    throw new Error('The Goal Run could not be refreshed.')
  }
  return response.json() as Promise<GoalRun>
}

export default function App() {
  const [goal, setGoal] = useState('')
  const [run, setRun] = useState<GoalRun | null>(null)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!run || run.state !== 'RUNNING') {
      return
    }

    let active = true
    const refresh = async () => {
      try {
        const refreshed = await readGoalRun(run.id)
        if (active) {
          setRun(refreshed)
          setError(null)
        }
      } catch (refreshError) {
        if (active) {
          setError(refreshError instanceof Error ? refreshError.message : 'The Goal Run could not be refreshed.')
        }
      }
    }
    const timer = window.setInterval(refresh, 1000)

    return () => {
      active = false
      window.clearInterval(timer)
    }
  }, [run])

  const createGoalRun = async (event: React.SubmitEvent<HTMLFormElement>) => {
    event.preventDefault()
    if (!goal.trim() || submitting || run?.state === 'RUNNING') {
      return
    }

    setSubmitting(true)
    setError(null)
    try {
      const response = await fetch('/api/goal-runs', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ goal: goal.trim() }),
      })
      if (!response.ok) {
        const problem = (await response.json().catch(() => ({}))) as ApiError
        throw new Error(problem.message ?? 'The Goal Run could not be started.')
      }
      setRun((await response.json()) as GoalRun)
      setGoal('')
    } catch (submitError) {
      setError(submitError instanceof Error ? submitError.message : 'The Goal Run could not be started.')
    } finally {
      setSubmitting(false)
    }
  }

  const isRunning = run?.state === 'RUNNING'

  return (
    <main className="mx-auto min-h-screen w-full max-w-5xl px-4 py-8 sm:px-8 sm:py-12">
      <header className="flex flex-col gap-5 border-b border-border pb-8 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <p className="font-mono text-xs uppercase tracking-[0.18em] text-muted">Goal orchestration</p>
          <h1 className="mt-3 max-w-2xl text-3xl font-semibold tracking-tight text-primary sm:text-4xl">
            One bounded run. One useful outcome.
          </h1>
        </div>
        <p className="font-mono text-xs text-muted">Private MVP</p>
      </header>

      <section hidden={isRunning} aria-labelledby="goal-heading" className="py-8 sm:py-10">
        <div className="mb-5">
          <p className="font-mono text-xs uppercase tracking-[0.16em] text-accent">New run</p>
          <h2 id="goal-heading" className="mt-2 text-xl font-medium text-primary">
            What outcome should the Orchestrator pursue?
          </h2>
        </div>

        <form onSubmit={createGoalRun} className="rounded-xl border border-border bg-surface p-4 sm:p-5">
          <label htmlFor="goal" className="sr-only">
            Goal
          </label>
          <textarea
            id="goal"
            value={goal}
            onChange={(event) => setGoal(event.target.value)}
            disabled={isRunning || submitting}
            maxLength={10000}
            rows={5}
            placeholder="Describe a concrete outcome..."
            className="block w-full resize-y border-0 bg-transparent text-base leading-7 text-primary placeholder:text-muted focus:outline-none disabled:cursor-not-allowed disabled:opacity-60"
          />
          <div className="mt-4 flex flex-col gap-3 border-t border-border pt-4 sm:flex-row sm:items-center sm:justify-between">
            <p className="text-sm text-muted">
              {isRunning ? 'Wait for the active Goal Run to finish.' : 'The Goal remains immutable after submission.'}
            </p>
            <button
              type="submit"
              disabled={!goal.trim() || isRunning || submitting}
              className="min-h-11 rounded-lg bg-accent px-5 py-2.5 text-sm font-semibold text-canvas transition-colors hover:bg-violet-300 disabled:cursor-not-allowed disabled:opacity-40"
            >
              {submitting ? 'Starting…' : 'Start Goal Run'}
            </button>
          </div>
        </form>
      </section>

      {error && (
        <p role="alert" className="mb-8 rounded-lg border border-danger/40 bg-danger/10 px-4 py-3 text-sm text-danger">
          {error}
        </p>
      )}

      <section aria-labelledby="run-heading" aria-live="polite" className="pb-12">
        <div className="mb-5 flex items-center justify-between gap-4">
          <div>
            <p className="font-mono text-xs uppercase tracking-[0.16em] text-muted">Current run</p>
            <h2 id="run-heading" className="mt-2 text-xl font-medium text-primary">
              {run ? 'Execution outcome' : 'No Goal Run yet'}
            </h2>
          </div>
          {run && <StateBadge state={run.state} />}
        </div>

        {run ? (
          <article className="rounded-xl border border-border bg-surface p-5 sm:p-6">
            <p className="font-mono text-xs text-muted">{run.id}</p>
            <p className="mt-5 max-w-3xl text-lg leading-8 text-primary">{run.goal}</p>

            {run.state === 'RUNNING' && (
              <p className="mt-6 border-t border-border pt-5 text-sm text-muted">
                The Orchestrator is evaluating the Goal.
              </p>
            )}

            {run.state === 'COMPLETED' && run.finalAnswer && (
              <div className="mt-6 border-t border-border pt-5">
                <p className="font-mono text-xs uppercase tracking-[0.16em] text-success">{run.outcome}</p>
                <p className="mt-3 max-w-3xl whitespace-pre-wrap text-base leading-7 text-primary">
                  {run.finalAnswer}
                </p>
              </div>
            )}

            {run.failureReason && (
              <p className="mt-6 border-t border-border pt-5 text-sm text-danger">{run.failureReason}</p>
            )}
          </article>
        ) : (
          <div className="rounded-xl border border-dashed border-border px-5 py-10 text-center text-sm text-muted">
            Submit a Goal to inspect its state and final answer here.
          </div>
        )}
      </section>
    </main>
  )
}

function StateBadge({ state }: { state: GoalRunState }) {
  const tone = {
    RUNNING: 'border-warning/40 bg-warning/10 text-warning',
    COMPLETED: 'border-success/40 bg-success/10 text-success',
    FAILED: 'border-danger/40 bg-danger/10 text-danger',
    LIMIT_REACHED: 'border-warning/40 bg-warning/10 text-warning',
    CANCELLED: 'border-border bg-canvas text-muted',
  }[state]

  return (
    <span className={`rounded-full border px-3 py-1 font-mono text-xs ${tone}`}>
      State: {state.replace('_', ' ')}
    </span>
  )
}
