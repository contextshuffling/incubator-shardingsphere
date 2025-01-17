/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.core.route.type.complex;

import org.apache.shardingsphere.core.exception.ShardingException;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.groupby.GroupBy;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.item.SelectItem;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.item.SelectItems;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.orderby.OrderBy;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.pagination.Pagination;
import org.apache.shardingsphere.core.optimize.sharding.statement.dml.ShardingSelectOptimizedStatement;
import org.apache.shardingsphere.core.parse.sql.segment.generic.TableSegment;
import org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.core.route.fixture.AbstractRoutingEngineTest;
import org.apache.shardingsphere.core.route.type.RoutingResult;
import org.apache.shardingsphere.core.route.type.RoutingUnit;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ComplexRoutingEngineTest extends AbstractRoutingEngineTest {
    
    @Test
    public void assertRoutingForBindingTables() {
        ShardingRule shardingRule = createBindingShardingRule();
        ShardingSelectOptimizedStatement optimizedStatement = new ShardingSelectOptimizedStatement(new SelectStatement(),
                new GroupBy(Collections.<OrderByItem>emptyList(), 0), new OrderBy(Collections.<OrderByItem>emptyList(), false), 
                new SelectItems(0, 0, false, Collections.<SelectItem>emptyList(), Collections.<TableSegment>emptyList(), null),
                new Pagination(null, null, Collections.emptyList()));
        ComplexRoutingEngine complexRoutingEngine = new ComplexRoutingEngine(shardingRule, Arrays.asList("t_order", "t_order_item"), optimizedStatement, createShardingConditions("t_order"));
        RoutingResult routingResult = complexRoutingEngine.route();
        List<RoutingUnit> tableUnitList = new ArrayList<>(routingResult.getRoutingUnits());
        assertThat(routingResult, instanceOf(RoutingResult.class));
        assertThat(routingResult.getRoutingUnits().size(), is(1));
        assertThat(tableUnitList.get(0).getDataSourceName(), is("ds_1"));
        assertThat(tableUnitList.get(0).getTableUnits().size(), is(1));
        assertThat(tableUnitList.get(0).getTableUnits().get(0).getActualTableName(), is("t_order_1"));
        assertThat(tableUnitList.get(0).getTableUnits().get(0).getLogicTableName(), is("t_order"));
    }
    
    @Test
    public void assertRoutingForShardingTableJoinBroadcastTable() {
        ShardingRule shardingRule = createBroadcastShardingRule();
        ShardingSelectOptimizedStatement optimizedStatement = new ShardingSelectOptimizedStatement(new SelectStatement(),
                new GroupBy(Collections.<OrderByItem>emptyList(), 0), new OrderBy(Collections.<OrderByItem>emptyList(), false), 
                new SelectItems(0, 0, false, Collections.<SelectItem>emptyList(), Collections.<TableSegment>emptyList(), null),
                new Pagination(null, null, Collections.emptyList()));
        ComplexRoutingEngine complexRoutingEngine = new ComplexRoutingEngine(shardingRule, Arrays.asList("t_order", "t_config"), optimizedStatement, createShardingConditions("t_order"));
        RoutingResult routingResult = complexRoutingEngine.route();
        List<RoutingUnit> tableUnitList = new ArrayList<>(routingResult.getRoutingUnits());
        assertThat(routingResult, instanceOf(RoutingResult.class));
        assertThat(routingResult.getRoutingUnits().size(), is(1));
        assertThat(tableUnitList.get(0).getDataSourceName(), is("ds_1"));
        assertThat(tableUnitList.get(0).getTableUnits().size(), is(1));
        assertThat(tableUnitList.get(0).getTableUnits().get(0).getActualTableName(), is("t_order_1"));
        assertThat(tableUnitList.get(0).getTableUnits().get(0).getLogicTableName(), is("t_order"));
    }
    
    @Test(expected = ShardingException.class)
    public void assertRoutingForNonLogicTable() {
        ComplexRoutingEngine complexRoutingEngine = new ComplexRoutingEngine(null, Collections.<String>emptyList(), null, createShardingConditions("t_order"));
        complexRoutingEngine.route();
    }
}
