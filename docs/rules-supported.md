# Supported UNO Rules

This project follows the local `Final_Project_UNO_rules_reference.md` behavior
with the simplifications listed below.

## Implemented

- Correct 108-card UNO-style deck:
  - four colors: red, yellow, green, blue
  - one 0 per color
  - two 1-9 cards per color
  - two Skip, Reverse, and Draw Two cards per color
  - four Wild cards
  - four Wild Draw Four cards
- Legal play validation:
  - color match
  - number match
  - action-type match
  - Wild and Wild Draw Four are playable
  - called wild color becomes the active color
- Skip:
  - next player loses their turn
- Reverse:
  - direction changes in games with three or more players
  - in two-player games, Reverse behaves like Skip
- Draw Two:
  - next player draws two cards and loses their turn
- Wild:
  - player chooses the next active color
- Wild Draw Four:
  - player chooses the next active color
  - next player draws four cards and loses their turn
- Draw/pass:
  - player may draw
  - if the drawn card is legal, the implementation allows immediate play
  - otherwise the turn passes
- UNO call:
  - one-card state is detected
  - humans can call UNO by adding `UNO` to a card play, such as `R5 UNO`
  - bots call UNO automatically when their play leaves one card
  - missed UNO call receives an immediate two-card penalty
- Round scoring:
  - number cards score face value
  - Skip, Reverse, Draw Two score 20
  - Wild and Wild Draw Four score 50
  - round winner receives points from opponents' remaining cards
- Multi-round target:
  - `--target-score N` continues rounds until one player reaches or exceeds `N`

## Documented Simplifications

- No Draw Two or Wild Draw Four stacking.
- No Wild Draw Four challenge rule.
- Starting discard action cards and wild cards are redrawn until a non-wild
  starting card is found; action effects are not applied at setup.
- UNO missed-call timing is simplified: the penalty is applied immediately when
  a play leaves one card without an UNO call.
- Bot strategy is simple priority-based play rather than advanced strategy.
- CLI is text-only.
