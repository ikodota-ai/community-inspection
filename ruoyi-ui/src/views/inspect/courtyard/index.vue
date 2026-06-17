<template>
  <div class="app-container">
    <el-form :inline="true" size="small">
      <el-form-item><el-button type="success" @click="openAdd" icon="el-icon-plus">新增网格</el-button></el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column label="排序" prop="sortOrder" width="80"/>
      <el-table-column label="网格名称" prop="courtyardName" min-width="180"/>
      <el-table-column label="状态" width="100">
        <template slot-scope="s"><el-tag size="small" :type="s.row.status==='0'?'success':'danger'">{{ s.row.status==='0'?'正常':'停用' }}</el-tag></template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template slot-scope="s">
          <el-button size="mini" @click="openEdit(s.row)">修改</el-button>
          <el-button size="mini" type="danger" @click="handleDelete(s.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog :title="isEdit?'修改网格':'新增网格'" :visible.sync="dialogVisible" width="400px">
      <el-form :model="form" label-width="80px" size="small">
        <el-form-item label="网格名称"><el-input v-model="form.courtyardName"/></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sortOrder" :min="0"/></el-form-item>
        <el-form-item label="状态"><el-select v-model="form.status"><el-option label="正常" value="0"/><el-option label="停用" value="1"/></el-select></el-form-item>
      </el-form>
      <span slot="footer"><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="submitForm">确定</el-button></span>
    </el-dialog>
  </div>
</template>

<script>
import request from '@/utils/request'

export default {
  name: 'InspectCourtyard',
  data() {
    return { loading: false, list: [], dialogVisible: false, isEdit: false,
      form: { courtyardId: null, courtyardName: '', sortOrder: 0, status: '0' }
    }
  },
  created() { this.handleQuery(); },
  methods: {
    handleQuery() { this.loading = true; request({ url:'/inspect/courtyard/list', method:'get' }).then(res => { this.list = res.data || []; this.loading = false; }); },
    openAdd() { this.isEdit = false; this.form = { courtyardId: null, courtyardName: '', sortOrder: this.list.length+1, status: '0' }; this.dialogVisible = true; },
    openEdit(row) { this.isEdit = true; this.form = { ...row }; this.dialogVisible = true; },
    submitForm() {
      const fn = this.isEdit
        ? () => request({ url:'/inspect/courtyard', method:'put', data:this.form })
        : () => request({ url:'/inspect/courtyard', method:'post', data:this.form });
      fn().then(() => { this.dialogVisible = false; this.$message.success('操作成功'); this.handleQuery(); });
    },
    handleDelete(row) {
      this.$confirm('确定删除该网格?', '提示', { type: 'warning' }).then(() => {
        request({ url:'/inspect/courtyard/'+row.courtyardId, method:'delete' }).then(() => {
          this.$message.success('删除成功'); this.handleQuery();
        });
      });
    }
  }
}
</script>
