<template>
  <div class="app-container">
    <el-form :inline="true" :model="queryParams" size="small">
      <el-form-item label="院落"><el-select v-model="queryParams.courtyardId" clearable @change="handleQuery" placeholder="全部"><el-option v-for="c in courtyards" :key="c.courtyardId" :label="c.courtyardName" :value="c.courtyardId"/></el-select></el-form-item>
      <el-form-item label="用户名"><el-input v-model="queryParams.userName" @keyup.enter.native="handleQuery" placeholder="搜索"/></el-form-item>
      <el-form-item><el-button type="primary" @click="handleQuery" icon="el-icon-search">搜索</el-button></el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column label="用户名" prop="userName" width="120"/>
      <el-table-column label="昵称" prop="nickName" width="120"/>
      <el-table-column label="手机号" prop="phonenumber" width="130"/>
      <el-table-column label="所属部门" prop="dept.deptName" width="120"/>
      <el-table-column label="当前院落" width="150">
        <template slot-scope="s">
          <el-tag v-if="s.row.courtyardId" size="small" type="success">{{ courtyardName(s.row.courtyardId) }}</el-tag>
          <el-tag v-else size="small" type="info">未分配</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="250">
        <template slot-scope="s">
          <el-select v-model="s.row._newCourtyardId" size="mini" placeholder="选择院落" style="width:130px" clearable>
            <el-option v-for="c in courtyards" :key="c.courtyardId" :label="c.courtyardName" :value="c.courtyardId"/>
          </el-select>
          <el-button size="mini" type="primary" @click="assign(s.row)" :disabled="s.row._newCourtyardId===undefined">分配</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="handleQuery"/>
  </div>
</template>

<script>
import { listUser, setCourtyard } from '@/api/inspect/user'
import { listCourtyard } from '@/api/inspect/dashboard'

export default {
  name: 'InspectUser',
  data() {
    return { loading: false, total: 0, list: [], courtyards: [],
      queryParams: { pageNum: 1, pageSize: 10, courtyardId: null, userName: null }
    }
  },
  created() { this.loadCourtyards(); this.handleQuery(); },
  methods: {
    loadCourtyards() { listCourtyard().then(res => { this.courtyards = res.data || []; }); },
    courtyardName(id) { const c = this.courtyards.find(x=>x.courtyardId===id); return c ? c.courtyardName : ''; },
    handleQuery() { this.loading = true; listUser(this.queryParams).then(res => {
      this.list = (res.rows||[]).map(r => ({ ...r, _newCourtyardId: undefined }));
      this.total = res.total; this.loading = false;
    }); },
    assign(row) {
      if (row._newCourtyardId === undefined) { this.$message.warning('请选择院落'); return; }
      setCourtyard({ userId: row.userId, courtyardId: row._newCourtyardId }).then(() => {
        row.courtyardId = row._newCourtyardId;
        row._newCourtyardId = undefined;
        this.$message.success('分配成功');
      });
    }
  }
}
</script>
