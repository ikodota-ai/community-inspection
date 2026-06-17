<template>
  <div class="app-container">
    <el-form :inline="true" :model="queryParams" size="small">
      <el-form-item label="网格"><el-select v-model="queryParams.courtyardId" clearable @change="handleQuery" placeholder="全部"><el-option v-for="c in courtyards" :key="c.courtyardId" :label="c.courtyardName" :value="c.courtyardId"/></el-select></el-form-item>
      <el-form-item><el-button type="primary" @click="handleQuery" icon="el-icon-search">搜索</el-button></el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column label="网格" prop="courtyardName" width="120"/>
      <el-table-column label="地址" prop="addressName" min-width="180"/>
      <el-table-column label="承租人" prop="tenantName" width="100"/>
      <el-table-column label="电话" prop="tenantPhone" width="130"/>
      <el-table-column label="最近巡查时间" prop="inspectTime" width="160"/>
      <el-table-column label="最近结果" width="90">
        <template slot-scope="s"><el-tag size="mini" :type="s.row.inspectResult==='pass'?'success':'danger'">{{ resultMap[s.row.inspectResult] }}</el-tag></template>
      </el-table-column>
    </el-table>
    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="handleQuery"/>
  </div>
</template>

<script>
import { listTenant } from '@/api/inspect/dashboard'
import { listCourtyard } from '@/api/inspect/dashboard'

export default {
  name: 'InspectTenant',
  data() {
    return {
      loading: false, total: 0, list: [], courtyards: [],
      queryParams: { pageNum: 1, pageSize: 10, courtyardId: null },
      resultMap: { pass: '合格', hazard: '存在隐患', rectified: '已整改' }
    }
  },
  created() { this.loadCourtyards(); this.handleQuery(); },
  methods: {
    loadCourtyards() { listCourtyard().then(res => { this.courtyards = res.data || []; }); },
    handleQuery() { this.loading = true; listTenant(this.queryParams).then(res => { this.list = res.rows; this.total = res.total; this.loading = false; }); }
  }
}
</script>
