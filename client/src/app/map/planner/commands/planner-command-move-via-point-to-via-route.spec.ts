import {List} from "immutable";
import {PlanLeg} from "../../../kpn/api/common/planner/plan-leg";
import {PlannerTestSetup} from "../context/planner-test-setup";
import {PlanFlagType} from "../plan/plan-flag-type";
import {PlanUtil} from "../plan/plan-util";
import {PlannerCommandAddLeg} from "./planner-command-add-leg";
import {PlannerCommandAddStartPoint} from "./planner-command-add-start-point";
import {PlannerCommandMoveViaPointToViaRoute} from "./planner-command-move-via-point-to-via-route";

describe("PlannerCommandMoveViaPointToViaRoute", () => {

  it("do and undo", () => {

    const setup = new PlannerTestSetup();

    const node1 = PlanUtil.planNodeWithCoordinate("1001", "01", [1, 1]);
    const node2 = PlanUtil.planNodeWithCoordinate("1002", "02", [2, 2]);
    const node3 = PlanUtil.planNodeWithCoordinate("1003", "03", [3, 3]);

    const legEnd1 = PlanUtil.legEndNode(+node1.nodeId);
    const legEnd2 = PlanUtil.legEndNode(+node2.nodeId);
    const legEnd3 = PlanUtil.legEndNode(+node3.nodeId);

    const oldLeg1 = new PlanLeg("12", "", legEnd1, legEnd2, node1, node2, 0, List());
    const oldLeg2 = new PlanLeg("23", "", legEnd2, legEnd3, node2, node3, 0, List());
    const newLeg = new PlanLeg("13", "", legEnd1, legEnd3, node1, node3, 0, List());

    setup.legs.add(oldLeg1);
    setup.legs.add(oldLeg2);
    setup.legs.add(newLeg);

    setup.context.execute(new PlannerCommandAddStartPoint(node1));
    setup.context.execute(new PlannerCommandAddLeg(oldLeg1.featureId));
    setup.context.execute(new PlannerCommandAddLeg(oldLeg2.featureId));

    setup.routeLayer.expectFlagCount(3);
    setup.routeLayer.expectFlagExists(PlanFlagType.Start, node1.featureId, [1, 1]);
    setup.routeLayer.expectFlagExists(PlanFlagType.Via, node2.featureId, [2, 2]);
    setup.routeLayer.expectFlagExists(PlanFlagType.End, node3.featureId, [3, 3]);
    setup.routeLayer.expectRouteLegExists("12", oldLeg1);
    setup.routeLayer.expectRouteLegExists("23", oldLeg2);

    expect(setup.context.plan.legs.size).toEqual(2);
    expect(setup.context.plan.legs.get(0).featureId).toEqual("12");
    expect(setup.context.plan.legs.get(0).sourceNode.nodeId).toEqual("1001");
    expect(setup.context.plan.legs.get(0).sinkNode.nodeId).toEqual("1002");
    expect(setup.context.plan.legs.get(1).featureId).toEqual("23");
    expect(setup.context.plan.legs.get(1).sourceNode.nodeId).toEqual("1002");
    expect(setup.context.plan.legs.get(1).sinkNode.nodeId).toEqual("1003");

    const command = new PlannerCommandMoveViaPointToViaRoute(
      oldLeg1.featureId,
      oldLeg2.featureId,
      newLeg.featureId,
      // new ViaRoute(10, 1),
      [5, 5]
    );
    setup.context.execute(command);

    setup.routeLayer.expectFlagCount(3);
    setup.routeLayer.expectFlagExists(PlanFlagType.Start, node1.featureId, [1, 1]);
    setup.routeLayer.expectFlagExists(PlanFlagType.Via, "10-1", [5, 5]);
    setup.routeLayer.expectFlagExists(PlanFlagType.End, node3.featureId, [3, 3]);
    setup.routeLayer.expectRouteLegExists("13", newLeg);

    expect(setup.context.plan.legs.size).toEqual(1);
    expect(setup.context.plan.legs.get(0).featureId).toEqual("13");
    expect(setup.context.plan.legs.get(0).sourceNode.nodeId).toEqual("1001");
    expect(setup.context.plan.legs.get(0).sinkNode.nodeId).toEqual("1003");

    command.undo(setup.context);

    setup.routeLayer.expectFlagCount(3);
    setup.routeLayer.expectFlagExists(PlanFlagType.Start, node1.featureId, [1, 1]);
    setup.routeLayer.expectFlagExists(PlanFlagType.Via, node2.featureId, [2, 2]);
    setup.routeLayer.expectFlagExists(PlanFlagType.End, node3.featureId, [3, 3]);
    setup.routeLayer.expectRouteLegExists("12", oldLeg1);
    setup.routeLayer.expectRouteLegExists("23", oldLeg2);

    expect(setup.context.plan.legs.size).toEqual(2);
    expect(setup.context.plan.legs.get(0).featureId).toEqual("12");
    expect(setup.context.plan.legs.get(0).sourceNode.nodeId).toEqual("1001");
    expect(setup.context.plan.legs.get(0).sinkNode.nodeId).toEqual("1002");
    expect(setup.context.plan.legs.get(1).featureId).toEqual("23");
    expect(setup.context.plan.legs.get(1).sourceNode.nodeId).toEqual("1002");
    expect(setup.context.plan.legs.get(1).sinkNode.nodeId).toEqual("1003");
  });

});